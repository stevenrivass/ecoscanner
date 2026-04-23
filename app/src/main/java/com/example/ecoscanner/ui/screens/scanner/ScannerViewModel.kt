package com.example.ecoscanner.ui.screens.scanner

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

class ScannerViewModel(
    private val repo: ScanRepository = ScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    // Evita procesar el mismo código muchas veces mientras ML Kit detecta
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
                val name = product.productNameEs
                    ?: product.productName
                    ?: "Producto desconocido"
                val origin = product.origins
                    ?: product.countries
                    ?: product.countriesTags?.firstOrNull()
                    ?: "Desconocido"

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
}