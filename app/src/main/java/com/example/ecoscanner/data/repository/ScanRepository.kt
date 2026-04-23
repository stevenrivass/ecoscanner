package com.example.ecoscanner.data.repository

import com.example.ecoscanner.data.network.OpenFoodFactsClient
import com.example.ecoscanner.data.network.ProductDto
import kotlin.math.*

class ScanRepository {

    companion object {
        // Factor de CO2 por km (transporte medio en camión). Ajustable.
        const val CO2_GRAMS_PER_KM = 0.137

        // Coordenadas aproximadas (capital / centro del país)
        private val COUNTRY_COORDS: Map<String, Pair<Double, Double>> = mapOf(
            // --- Europa ---
            "españa"         to (40.4168 to  -3.7038),  // Madrid
            "spain"          to (40.4168 to  -3.7038),
            "francia"        to (48.8566 to   2.3522),  // París
            "france"         to (48.8566 to   2.3522),
            "italia"         to (41.9028 to  12.4964),  // Roma
            "italy"          to (41.9028 to  12.4964),
            "portugal"       to (38.7223 to  -9.1393),  // Lisboa
            "alemania"       to (52.5200 to  13.4050),  // Berlín
            "germany"        to (52.5200 to  13.4050),
            "reino unido"    to (51.5074 to  -0.1278),  // Londres
            "united kingdom" to (51.5074 to  -0.1278),
            "países bajos"   to (52.3676 to   4.9041),  // Ámsterdam
            "netherlands"    to (52.3676 to   4.9041),
            "bélgica"        to (50.8503 to   4.3517),  // Bruselas
            "belgium"        to (50.8503 to   4.3517),
            "suiza"          to (46.9480 to   7.4474),  // Berna
            "switzerland"    to (46.9480 to   7.4474),
            "polonia"        to (52.2297 to  21.0122),  // Varsovia
            "poland"         to (52.2297 to  21.0122),
            "grecia"         to (37.9838 to  23.7275),  // Atenas
            "greece"         to (37.9838 to  23.7275),

            // --- África ---
            "marruecos"      to (33.9716 to  -6.8498),  // Rabat
            "morocco"        to (33.9716 to  -6.8498),
            "túnez"          to (36.8065 to  10.1815),  // Túnez
            "tunisia"        to (36.8065 to  10.1815),
            "egipto"         to (30.0444 to  31.2357),  // El Cairo
            "egypt"          to (30.0444 to  31.2357),

            // --- América ---
            "perú"           to (-12.0464 to -77.0428), // Lima
            "peru"           to (-12.0464 to -77.0428),
            "chile"          to (-33.4489 to -70.6693), // Santiago
            "argentina"      to (-34.6037 to -58.3816), // Buenos Aires
            "brasil"         to (-15.7939 to -47.8828), // Brasilia
            "brazil"         to (-15.7939 to -47.8828),
            "méxico"         to ( 19.4326 to -99.1332), // CDMX
            "mexico"         to ( 19.4326 to -99.1332),
            "colombia"       to (  4.7110 to -74.0721), // Bogotá
            "ecuador"        to ( -0.1807 to -78.4678), // Quito
            "equador"        to ( -0.1807 to -78.4678), // catalán
            "estados unidos" to ( 38.9072 to -77.0369), // Washington DC
            "united states"  to ( 38.9072 to -77.0369),
            "usa"            to ( 38.9072 to -77.0369),
            "canadá"         to ( 45.4215 to -75.6972), // Ottawa
            "canada"         to ( 45.4215 to -75.6972),

            // --- Asia ---
            "china"          to ( 39.9042 to 116.4074), // Pekín
            "japón"          to ( 35.6762 to 139.6503), // Tokio
            "japan"          to ( 35.6762 to 139.6503),
            "india"          to ( 28.6139 to  77.2090), // Nueva Delhi
            "turquía"        to ( 39.9334 to  32.8597), // Ankara
            "turkey"         to ( 39.9334 to  32.8597)
        )
    }

    // ---------- API ----------
    // Llamada a Open Food Facts.
    // Devuelve el producto o null si la API responde status=0 (no encontrado).
    suspend fun fetchProduct(barcode: String): ProductDto? {
        val response = OpenFoodFactsClient.api.getProduct(barcode)
        return if (response.status == 1) response.product else null
    }

    // ---------- Geo ----------
    // Devuelve (lat, lon) del país o null si no está en el mapa.
    // Si el string trae varios países separados por coma, prueba todos
    // y devuelve el PRIMERO conocido.
    fun coordsForCountry(country: String?): Pair<Double, Double>? {
        if (country.isNullOrBlank()) return null

        val candidates = country
            .split(",", ";", "/", "-")         // separadores típicos
            .map {
                it.trim().lowercase()
                    .removePrefix("en:")       // "en:france" -> "france"
                    .removePrefix("es:")
                    .removePrefix("fr:")
                    .removePrefix("it:")
            }
            .filter { it.isNotBlank() }

        for (c in candidates) {
            COUNTRY_COORDS[c]?.let { return it }
        }
        return null
    }

    // Distancia Haversine en km
    fun haversineKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // radio Tierra km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun co2Grams(distanceKm: Double): Double = distanceKm * CO2_GRAMS_PER_KM
}