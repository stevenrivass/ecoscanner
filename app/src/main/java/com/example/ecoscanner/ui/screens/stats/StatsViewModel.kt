package com.example.ecoscanner.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.repository.FirestoreScanRepository
import com.example.ecoscanner.model.ScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data object Empty : StatsUiState
    data class Success(
        val totalScans: Int,
        val totalCo2Emitted: Double,         // gramos
        val totalCo2Saved: Double,            // gramos
        val totalDistanceKm: Double,
        val carTripsEquivalent: Int,          // CO2 emitido / 1200 g (viaje urbano medio)
        val savedCarTrips: Int,               // CO2 ahorrado / 1200 g
        val topPolluters: List<ScanRecord>    // 3 escaneos con más CO2
    ) : StatsUiState
    data class Error(val message: String) : StatsUiState
}

class StatsViewModel(
    private val repo: FirestoreScanRepository = FirestoreScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    companion object {
        // 1 viaje urbano medio en coche ≈ 10 km × 120 g/km = 1200 g CO2
        const val CO2_PER_CAR_TRIP_GRAMS = 1200.0
    }

    init {
        load()
    }

    fun load() {
        _state.value = StatsUiState.Loading
        viewModelScope.launch {
            repo.getAllScans()
                .onSuccess { scans ->
                    if (scans.isEmpty()) {
                        _state.value = StatsUiState.Empty
                    } else {
                        _state.value = computeStats(scans)
                    }
                }
                .onFailure {
                    _state.value = StatsUiState.Error(
                        it.localizedMessage ?: "Error desconegut"
                    )
                }
        }
    }

    private fun computeStats(scans: List<ScanRecord>): StatsUiState.Success {
        val totalEmitted = scans.sumOf { it.co2Grams }
        val totalSaved = scans.sumOf { it.co2SavedGrams }
        val totalDistance = scans.sumOf { it.distanceKm }

        val carTrips = (totalEmitted / CO2_PER_CAR_TRIP_GRAMS).toInt()
        val savedCarTrips = (totalSaved / CO2_PER_CAR_TRIP_GRAMS).toInt()

        val top3 = scans.sortedByDescending { it.co2Grams }.take(3)

        return StatsUiState.Success(
            totalScans = scans.size,
            totalCo2Emitted = totalEmitted,
            totalCo2Saved = totalSaved,
            totalDistanceKm = totalDistance,
            carTripsEquivalent = carTrips,
            savedCarTrips = savedCarTrips,
            topPolluters = top3
        )
    }
}