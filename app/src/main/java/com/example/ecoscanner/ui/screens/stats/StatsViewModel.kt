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
        val totalCo2Emitted: Double,
        val totalCo2Saved: Double,
        val totalDistanceKm: Double,
        val topPolluters: List<ScanRecord>,
        // Nuevas equivalencias
        val equivalences: Equivalences,
        // Sistema de niveles (basado en CO2 ahorrado)
        val level: EcoLevel,
        val progressToNextLevel: Float,    // 0.0 - 1.0
        val co2ToNextLevel: Double          // gramos que faltan
    ) : StatsUiState
    data class Error(val message: String) : StatsUiState
}

// Equivalencias del CO₂ emitido a otras métricas
data class Equivalences(
    val carTrips: Int,        // viajes urbanos en coche (1.200 g/viaje)
    val treesNeeded: Double,   // árboles necesarios para absorber este CO₂ en 1 año
    val flightMinutes: Int,    // minutos de vuelo equivalente (90 g/min)
    val ledHours: Int,         // horas de bombilla LED (4 g/h)
    val phoneCharges: Int      // cargas de móvil (8 g/carga)
)

// Niveles eco según CO2 ahorrado (en gramos)
enum class EcoLevel(
    val displayName: String,
    val emoji: String,
    val minCo2Saved: Double,
    val maxCo2Saved: Double,
    val description: String
) {
    LLAVOR(
        displayName = "Llavor",
        emoji = "🌱",
        minCo2Saved = 0.0,
        maxCo2Saved = 1_000.0,
        description = "Acabes de començar el teu camí eco"
    ),
    BROT(
        displayName = "Brot",
        emoji = "🌿",
        minCo2Saved = 1_000.0,
        maxCo2Saved = 5_000.0,
        description = "Estàs creant bons hàbits"
    ),
    ARBRET(
        displayName = "Arbret",
        emoji = "🌳",
        minCo2Saved = 5_000.0,
        maxCo2Saved = 20_000.0,
        description = "El teu impacte ja es nota"
    ),
    ARBRE(
        displayName = "Arbre",
        emoji = "🌲",
        minCo2Saved = 20_000.0,
        maxCo2Saved = 50_000.0,
        description = "Eres un referent eco"
    ),
    BOSC(
        displayName = "Bosc",
        emoji = "🌍",
        minCo2Saved = 50_000.0,
        maxCo2Saved = Double.MAX_VALUE,
        description = "Ets una llegenda del consum sostenible"
    );

    fun next(): EcoLevel? = when (this) {
        LLAVOR -> BROT
        BROT -> ARBRET
        ARBRET -> ARBRE
        ARBRE -> BOSC
        BOSC -> null
    }

    companion object {
        fun fromCo2Saved(grams: Double): EcoLevel {
            return values().first { grams >= it.minCo2Saved && grams < it.maxCo2Saved }
        }
    }
}

class StatsViewModel(
    private val repo: FirestoreScanRepository = FirestoreScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    companion object {
        // Factores de conversión de CO2 a equivalencias (todos en gramos)
        const val CO2_PER_CAR_TRIP = 1_200.0       // viaje urbano de 10 km
        const val CO2_PER_TREE_YEAR = 21_000.0     // 21 kg/año por árbol
        const val CO2_PER_FLIGHT_MIN = 90.0        // 90 g por minuto de vuelo por pasajero
        const val CO2_PER_LED_HOUR = 4.0           // bombilla LED 10W durante 1h
        const val CO2_PER_PHONE_CHARGE = 8.0       // carga completa de móvil
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

        val equivalences = Equivalences(
            carTrips = (totalEmitted / CO2_PER_CAR_TRIP).toInt(),
            treesNeeded = totalEmitted / CO2_PER_TREE_YEAR,
            flightMinutes = (totalEmitted / CO2_PER_FLIGHT_MIN).toInt(),
            ledHours = (totalEmitted / CO2_PER_LED_HOUR).toInt(),
            phoneCharges = (totalEmitted / CO2_PER_PHONE_CHARGE).toInt()
        )

        val level = EcoLevel.fromCo2Saved(totalSaved)
        val nextLevel = level.next()

        val progress: Float
        val toNext: Double

        if (nextLevel != null) {
            val rangeSize = level.maxCo2Saved - level.minCo2Saved
            val advanceInRange = totalSaved - level.minCo2Saved
            progress = (advanceInRange / rangeSize).toFloat().coerceIn(0f, 1f)
            toNext = (nextLevel.minCo2Saved - totalSaved).coerceAtLeast(0.0)
        } else {
            progress = 1f
            toNext = 0.0
        }

        val top3 = scans.sortedByDescending { it.co2Grams }.take(3)

        return StatsUiState.Success(
            totalScans = scans.size,
            totalCo2Emitted = totalEmitted,
            totalCo2Saved = totalSaved,
            totalDistanceKm = totalDistance,
            topPolluters = top3,
            equivalences = equivalences,
            level = level,
            progressToNextLevel = progress,
            co2ToNextLevel = toNext
        )
    }
}