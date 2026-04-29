package com.example.ecoscanner.ui.screens.calculation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.repository.FirestoreScanRepository
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
        val saved: Boolean
    ) : CalculationUiState
    data class Error(val message: String) : CalculationUiState
}

class CalculationViewModel(
    private val repo: ScanRepository = ScanRepository(),
    private val firestore: FirestoreScanRepository = FirestoreScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<CalculationUiState>(CalculationUiState.Loading)
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    // Para evitar guardar dos veces el mismo escaneo si Compose recompone
    private var alreadyProcessed = false

    fun process(
        productName: String,
        origin: String,
        imageUrl: String?,
        userLat: Double,
        userLon: Double
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

        // Mostramos resultado primero (UX rápida)
        _state.value = CalculationUiState.Success(
            productName = productName,
            origin = origin,
            imageUrl = imageUrl,
            distanceKm = km,
            co2Grams = co2,
            co2SavedGrams = co2Saved,
            saved = false
        )

        // Y luego guardamos en Firestore en background
        viewModelScope.launch {
            val record = ScanRecord(
                productName = productName,
                originCountry = origin,
                imageUrl = imageUrl,
                distanceKm = km,
                co2Grams = co2,
                co2SavedGrams = co2Saved
            )
            firestore.saveScan(record)
                .onSuccess {
                    val current = _state.value
                    if (current is CalculationUiState.Success) {
                        _state.value = current.copy(saved = true)
                    }
                }
                .onFailure {
                    // No bloqueamos la UI; el cálculo se ve, solo no se guardó
                    android.util.Log.e("CalculationVM", "Error guardando en Firestore", it)
                }
        }
    }
}