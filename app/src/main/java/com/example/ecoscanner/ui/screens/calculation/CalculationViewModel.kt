package com.example.ecoscanner.ui.screens.calculation

import androidx.lifecycle.ViewModel
import com.example.ecoscanner.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface CalculationUiState {
    data object Loading : CalculationUiState
    data class Success(
        val productName: String,
        val origin: String,
        val imageUrl: String?,
        val distanceKm: Double,
        val co2Grams: Double
    ) : CalculationUiState
    data class Error(val message: String) : CalculationUiState
}

class CalculationViewModel(
    private val repo: ScanRepository = ScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<CalculationUiState>(CalculationUiState.Loading)
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    fun process(
        productName: String,
        origin: String,
        imageUrl: String?,
        userLat: Double,
        userLon: Double
    ) {
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

        _state.value = CalculationUiState.Success(
            productName = productName,
            origin = origin,
            imageUrl = imageUrl,
            distanceKm = km,
            co2Grams = co2
        )
    }
}