package com.example.ecoscanner.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Loading : ScanUiState
    data class Success(
        val productName: String,
        val origin: String,
        val imageUrl: String?,
        val userLat: Double,
        val userLon: Double
    ) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class HomeViewModel(
    private val repo: ScanRepository = ScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private var lastBarcode: String? = null

    fun onBarcodeScanned(barcode: String, userLat: Double?, userLon: Double?) {
        if (barcode == lastBarcode) return
        lastBarcode = barcode

        if (userLat == null || userLon == null) {
            _state.value = ScanUiState.Error("No se pudo obtener la ubicación del usuario.")
            return
        }

        _state.value = ScanUiState.Loading
        viewModelScope.launch {
            try {
                val product = repo.fetchProduct(barcode)
                if (product == null) {
                    _state.value = ScanUiState.Error("Producto no encontrado ($barcode).")
                    return@launch
                }

                val name = listOfNotNull(
                    product.productNameEs,
                    product.productName
                ).firstOrNull { it.isNotBlank() } ?: "Producto desconocido"

                // Probamos múltiples campos en orden, ignorando vacíos
                val origin = pickFirstNonBlank(
                    product.origins,
                    product.countries,
                    product.countriesTags?.joinToString(",")
                )

                if (origin == null) {
                    _state.value = ScanUiState.Error(
                        "Aquest producte no té informació d'origen a Open Food Facts. " +
                                "Prova amb un altre producte."
                    )
                    return@launch
                }

                _state.value = ScanUiState.Success(
                    productName = name,
                    origin = origin,
                    imageUrl = product.imageFrontUrl ?: product.imageUrl,
                    userLat = userLat,
                    userLon = userLon
                )
            } catch (e: Exception) {
                _state.value = ScanUiState.Error("Error de red: ${e.localizedMessage}")
            }
        }
    }

    fun reset() {
        lastBarcode = null
        _state.value = ScanUiState.Idle
    }

    // Devuelve el primer string que no sea null ni esté en blanco
    private fun pickFirstNonBlank(vararg candidates: String?): String? {
        return candidates.firstOrNull { !it.isNullOrBlank() }
    }
}