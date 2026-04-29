package com.example.ecoscanner.model

import com.google.firebase.Timestamp

data class ScanRecord(
    val productName: String = "",
    val originCountry: String = "",
    val imageUrl: String? = null,
    val distanceKm: Double = 0.0,
    val co2Grams: Double = 0.0,
    val co2SavedGrams: Double = 0.0,
    val scannedAt: Timestamp? = null
)