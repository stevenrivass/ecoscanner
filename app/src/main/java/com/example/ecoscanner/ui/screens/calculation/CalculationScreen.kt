package com.example.ecoscanner.ui.screens.calculation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ecoscanner.ui.navigation.Routes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationScreen(
    navController: NavHostController,
    viewModel: CalculationViewModel = viewModel()
) {
    val prev = navController.previousBackStackEntry?.savedStateHandle
    val productName = prev?.get<String>("productName")
    val origin = prev?.get<String>("origin")
    val imageUrl = prev?.get<String>("imageUrl")
    val userLat = prev?.get<Double>("userLat")
    val userLon = prev?.get<Double>("userLon")

    LaunchedEffect(productName, origin, userLat, userLon) {
        if (productName != null && origin != null && userLat != null && userLon != null) {
            viewModel.process(
                productName = productName,
                origin = origin,
                imageUrl = imageUrl,
                userLat = userLat,
                userLon = userLon,
                scannedProductDto = com.example.ecoscanner.data.repository.ScanContext.lastScannedProduct
            )
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Si la decisión está tomada (confirmada o cancelada) → mostrar Snackbar y volver a Home
    LaunchedEffect(state) {
        val s = state
        if (s is CalculationUiState.Success) {
            when (s.purchaseDecision) {
                is PurchaseDecision.Confirmed -> {
                    snackbarHostState.showSnackbar(
                        message = "Compra registrada al teu historial",
                        duration = SnackbarDuration.Short
                    )
                    delay(800)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = false }
                    }
                }
                is PurchaseDecision.Cancelled -> {
                    snackbarHostState.showSnackbar(
                        message = "Compra cancel·lada",
                        duration = SnackbarDuration.Short
                    )
                    delay(800)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = false }
                    }
                }
                else -> { /* no-op */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NearChoice",
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when (val s = state) {
                is CalculationUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
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
                    // ----- Imagen producto -----
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
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

                    // ----- Detalles -----
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

                    // ----- Card de alternativas Km 0 -----
                    if (!s.isSpanish) {
                        Km0Card(km0State = s.km0State)
                    }

                    // ----- Botones de decisión -----
                    PurchaseDecisionButtons(
                        decision = s.purchaseDecision,
                        isSpanish = s.isSpanish,
                        onConfirm = { viewModel.confirmPurchase() },
                        onCancel = { viewModel.cancelPurchase() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ---------- Botones de decisión ----------

@Composable
private fun PurchaseDecisionButtons(
    decision: PurchaseDecision,
    isSpanish: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val isLoading = decision is PurchaseDecision.Saving
    val isPending = decision is PurchaseDecision.Pending

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mensaje contextual
        Text(
            text = if (isSpanish) {
                "Vols comprar aquest producte?"
            } else {
                "Vols comprar-lo o triaràs una alternativa local?"
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Botón confirmar
        Button(
            onClick = onConfirm,
            enabled = isPending,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sí, compro aquest producte",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botón cancelar
        OutlinedButton(
            onClick = onCancel,
            enabled = isPending,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Filled.Cancel, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isSpanish) "No el compro" else "Buscaré una alternativa local"
            )
        }

        // Mensaje de error si falla el guardado
        if (decision is PurchaseDecision.Error) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Error: ${decision.message}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ---------- Card Km 0 (idéntica a la fase anterior) ----------

@Composable
private fun Km0Card(km0State: Km0State) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Recommend,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Alternatives Km 0",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (km0State) {
                is Km0State.Idle, is Km0State.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Buscant productes locals…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                is Km0State.Empty -> {
                    Text(
                        text = "No hem trobat alternatives locals per a aquesta categoria. " +
                                "Pots buscar productes amb origen 'Espanya' al supermercat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                is Km0State.Success -> {
                    Text(
                        text = "Aquests productes locals tenen una petjada de carboni molt menor:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    km0State.suggestions.forEachIndexed { index, sug ->
                        Km0SuggestionRow(
                            suggestion = sug,
                            isTop = index == 0
                        )
                        if (index < km0State.suggestions.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Km0SuggestionRow(
    suggestion: Km0SuggestionUi,
    isTop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTop)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTop) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!suggestion.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = suggestion.imageUrl,
                    contentDescription = suggestion.productName,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Recommend,
                        contentDescription = null
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isTop) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = suggestion.productName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                }
                if (!suggestion.brand.isNullOrBlank()) {
                    Text(
                        text = suggestion.brand,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Estalvi: ${"%.0f".format(suggestion.co2SavedVsScanned)} g CO₂",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    suggestion.matchScore >= 70 -> MaterialTheme.colorScheme.primary
                    suggestion.matchScore >= 50 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                }
            ) {
                Text(
                    text = "${suggestion.matchScore}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ---------- InfoRow ----------

@Composable
private fun InfoRow(
    icon: ImageVector,
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