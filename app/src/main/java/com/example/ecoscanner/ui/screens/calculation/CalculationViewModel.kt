package com.example.ecoscanner.ui.screens.calculation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.network.ProductDto
import com.example.ecoscanner.data.repository.FirestoreScanRepository
import com.example.ecoscanner.data.repository.Km0MatchingService
import com.example.ecoscanner.data.repository.ScanRepository
import com.example.ecoscanner.model.ScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CalculationUiState {
    data object Loading : CalculationUiState
    data class Success(
        val productName: String,
        val origin: String,
        val imageUrl: String?,
        val distanceKm: Double,
        val co2Grams: Double,
        val co2SavedGrams: Double,
        val isSpanish: Boolean,
        val km0State: Km0State,
        val purchaseDecision: PurchaseDecision
    ) : CalculationUiState
    data class Error(val message: String) : CalculationUiState
}

sealed interface Km0State {
    data object Idle : Km0State
    data object Loading : Km0State
    data class Success(val suggestions: List<Km0SuggestionUi>) : Km0State
    data object Empty : Km0State
}

sealed interface PurchaseDecision {
    data object Pending : PurchaseDecision           // No ha decidido aún
    data object Saving : PurchaseDecision             // Está guardando en Firestore
    data object Confirmed : PurchaseDecision          // Confirmó compra y ya está guardado
    data object Cancelled : PurchaseDecision          // No comprado, no se guardó
    data class Error(val message: String) : PurchaseDecision
}

data class Km0SuggestionUi(
    val productName: String,
    val brand: String?,
    val imageUrl: String?,
    val origin: String,
    val matchScore: Int,
    val matchReason: String,
    val estimatedCo2Grams: Double,
    val co2SavedVsScanned: Double
)

class CalculationViewModel(
    private val repo: ScanRepository = ScanRepository(),
    private val firestore: FirestoreScanRepository = FirestoreScanRepository(),
    private val km0Service: Km0MatchingService = Km0MatchingService()
) : ViewModel() {

    private val _state = MutableStateFlow<CalculationUiState>(CalculationUiState.Loading)
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    private var alreadyProcessed = false

    fun process(
        productName: String,
        origin: String,
        imageUrl: String?,
        userLat: Double,
        userLon: Double,
        scannedProductDto: ProductDto? = null
    ) {
        if (alreadyProcessed) return
        alreadyProcessed = true

        val coords = repo.coordsForCountry(origin)
        if (coords == null) {
            _state.value = CalculationUiState.Error(
                "No podem calcular: origen desconegut ($origin)."
            )
            return
        }
        val (originLat, originLon) = coords
        val km = repo.haversineKm(userLat, userLon, originLat, originLon)
        val co2 = repo.co2Grams(km)
        val co2Saved = repo.co2SavedGrams(userLat, userLon, km)
        val isSpanish = isOriginSpanish(origin)

        _state.value = CalculationUiState.Success(
            productName = productName,
            origin = origin,
            imageUrl = imageUrl,
            distanceKm = km,
            co2Grams = co2,
            co2SavedGrams = co2Saved,
            isSpanish = isSpanish,
            km0State = if (isSpanish) Km0State.Idle else Km0State.Loading,
            purchaseDecision = PurchaseDecision.Pending
        )

        // Si no es español, buscamos alternativas locales en paralelo
        if (!isSpanish && scannedProductDto != null) {
            viewModelScope.launch {
                searchKm0Alternatives(scannedProductDto, userLat, userLon, co2)
            }
        }
    }

    // El usuario confirma que SÍ compra el producto escaneado → guardamos en Firestore
    fun confirmPurchase() {
        val current = _state.value
        if (current !is CalculationUiState.Success) return
        if (current.purchaseDecision != PurchaseDecision.Pending) return

        _state.value = current.copy(purchaseDecision = PurchaseDecision.Saving)

        viewModelScope.launch {
            val record = ScanRecord(
                productName = current.productName,
                originCountry = current.origin,
                imageUrl = current.imageUrl,
                distanceKm = current.distanceKm,
                co2Grams = current.co2Grams,
                co2SavedGrams = current.co2SavedGrams
            )
            firestore.saveScan(record)
                .onSuccess {
                    val now = _state.value
                    if (now is CalculationUiState.Success) {
                        _state.value = now.copy(purchaseDecision = PurchaseDecision.Confirmed)
                    }
                }
                .onFailure {
                    val now = _state.value
                    if (now is CalculationUiState.Success) {
                        _state.value = now.copy(
                            purchaseDecision = PurchaseDecision.Error(
                                it.localizedMessage ?: "Error guardant la compra"
                            )
                        )
                    }
                }
        }
    }

    // El usuario decide NO comprar (porque va a buscar alternativa local o no le interesa)
    fun cancelPurchase() {
        val current = _state.value
        if (current !is CalculationUiState.Success) return
        if (current.purchaseDecision != PurchaseDecision.Pending) return

        _state.value = current.copy(purchaseDecision = PurchaseDecision.Cancelled)
    }

    private suspend fun searchKm0Alternatives(
        scanned: ProductDto,
        userLat: Double,
        userLon: Double,
        scannedCo2: Double
    ) {
        try {
            val suggestions = km0Service.findLocalAlternatives(scanned)

            if (suggestions.isEmpty()) {
                updateKm0State(Km0State.Empty)
                return
            }

            val spainCoords = repo.coordsForCountry("España") ?: return
            val (spainLat, spainLon) = spainCoords
            val distanceToSpain = repo.haversineKm(userLat, userLon, spainLat, spainLon)
            val co2Spain = repo.co2Grams(distanceToSpain)

            val ui = suggestions.map { sug ->
                Km0SuggestionUi(
                    productName = sug.product.productNameEs
                        ?: sug.product.productName
                        ?: "Producte",
                    brand = sug.product.brands?.split(",")?.firstOrNull()?.trim(),
                    imageUrl = sug.product.imageFrontUrl ?: sug.product.imageUrl,
                    origin = "Espanya",
                    matchScore = sug.matchScore,
                    matchReason = sug.matchReason,
                    estimatedCo2Grams = co2Spain,
                    co2SavedVsScanned = (scannedCo2 - co2Spain).coerceAtLeast(0.0)
                )
            }

            updateKm0State(Km0State.Success(ui))
        } catch (e: Exception) {
            android.util.Log.e("CalculationVM", "Error buscant Km0", e)
            updateKm0State(Km0State.Empty)
        }
    }

    private fun updateKm0State(newKm0State: Km0State) {
        val current = _state.value
        if (current is CalculationUiState.Success) {
            _state.value = current.copy(km0State = newKm0State)
        }
    }

    private fun isOriginSpanish(origin: String): Boolean {
        val lower = origin.lowercase()
        return lower.contains("españa") ||
                lower.contains("spain") ||
                lower.contains("espanya") ||
                lower.contains("en:spain") ||
                lower.contains("es:espana")
    }
}