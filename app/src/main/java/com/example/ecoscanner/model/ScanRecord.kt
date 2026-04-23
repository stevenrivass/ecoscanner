package com.example.ecoscanner.model


data class ScanRecord(
    val productName: String,
    val originCountry: String,
    val imageUrl: String?,
    val distanceKm: Double,
    val co2Grams: Double
)