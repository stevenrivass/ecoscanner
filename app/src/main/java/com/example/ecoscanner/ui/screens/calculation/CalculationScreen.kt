package com.example.ecoscanner.ui.screens.calculation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ecoscanner.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(
    navController: NavHostController,
    viewModel: CalculationViewModel = viewModel()
) {
    // Recuperamos los datos que ScannerScreen dejó en el savedStateHandle anterior
    val prev = navController.previousBackStackEntry?.savedStateHandle
    val productName = prev?.get<String>("productName")
    val origin = prev?.get<String>("origin")
    val imageUrl = prev?.get<String>("imageUrl")
    val userLat = prev?.get<Double>("userLat")
    val userLon = prev?.get<Double>("userLon")

    LaunchedEffect(productName, origin, userLat, userLon) {
        if (productName != null && origin != null && userLat != null && userLon != null) {
            viewModel.process(productName, origin, imageUrl, userLat, userLon)
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EcoScanner",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Enrere")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when (val s = state) {
                is CalculationUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is CalculationUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = s.message,
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is CalculationUiState.Success -> {
                    // Imagen del producto
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!s.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = s.imageUrl,
                                    contentDescription = s.productName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Fastfood,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(96.dp)
                                    )
                                    Text(
                                        s.productName,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }

                    // Detalles
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Default.Fastfood,
                                label = "Nom producte",
                                value = s.productName
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            InfoRow(
                                icon = Icons.Filled.LocationOn,
                                label = "Distància",
                                value = "${"%.0f".format(s.distanceKm)} km (${s.origin})"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(
                                    Icons.Filled.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        "Petjada de Carboni",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        "${"%.1f".format(s.co2Grams)} g CO₂",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate(Routes.SCANNER) {
                        popUpTo(Routes.SCANNER) { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Tornar a l'inici", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}