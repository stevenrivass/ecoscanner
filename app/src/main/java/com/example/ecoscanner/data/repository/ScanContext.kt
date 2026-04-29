package com.example.ecoscanner.data.repository

import com.example.ecoscanner.data.network.ProductDto

// Almacenamiento temporal del último DTO escaneado.
// Se usa solo para pasar el producto entre HomeScreen y CalculationScreen.
// No es persistente — se pierde al cerrar la app, lo cual es correcto.
object ScanContext {
    var lastScannedProduct: ProductDto? = null
}