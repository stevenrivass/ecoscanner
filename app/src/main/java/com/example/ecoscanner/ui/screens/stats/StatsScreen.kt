package com.example.ecoscanner.ui.screens.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecoscanner.model.ScanRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadístiques",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is StatsUiState.Loading -> LoadingView()
                is StatsUiState.Empty -> EmptyView()
                is StatsUiState.Error -> ErrorView(s.message) { viewModel.load() }
                is StatsUiState.Success -> StatsContent(s)
            }
        }
    }
}

// ---------- Estados ----------

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun EmptyView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.BarChart,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Encara no hi ha estadístiques",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Escaneja productes per veure el teu impacte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No s'han pogut carregar les estadístiques",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Tornar a provar") }
    }
}

// ---------- Contenido principal ----------

@Composable
private fun StatsContent(s: StatsUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ----- Card de NIVEL ECO (lo más llamativo, arriba) -----
        EcoLevelCard(
            level = s.level,
            progress = s.progressToNextLevel,
            co2ToNext = s.co2ToNextLevel,
            totalSaved = s.totalCo2Saved
        )

        Text(
            text = "Has escanejat ${s.totalScans} producte${if (s.totalScans == 1) "" else "s"}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ----- Cards principales: emitido y ahorrado -----
        BigStatCard(
            icon = Icons.Filled.Whatshot,
            title = "CO₂ emès",
            mainValue = "${"%.0f".format(s.totalCo2Emitted)} g",
            subtitle = "Per culpa del transport dels productes",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )

        BigStatCard(
            icon = Icons.Filled.Eco,
            title = "CO₂ estalviat",
            mainValue = "${"%.0f".format(s.totalCo2Saved)} g",
            subtitle = "Triant productes més propers",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // ----- SECCIÓN: EQUIVALÈNCIES VISUALS -----
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Equivalències del CO₂ emès",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Per fer-te una idea de l'impacte real:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Grid de 2 columnas, 3 filas (5 equivalencias + distancia)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EquivalenceCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.DirectionsCar,
                value = "${s.equivalences.carTrips}",
                label = "viatges urbans en cotxe",
                tint = Color(0xFFE57373)
            )
            EquivalenceCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Park,
                value = "%.1f".format(s.equivalences.treesNeeded),
                label = "arbres per absorbir-ho en 1 any",
                tint = Color(0xFF66BB6A)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EquivalenceCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Flight,
                value = "${s.equivalences.flightMinutes}",
                label = "minuts de vol comercial",
                tint = Color(0xFF42A5F5)
            )
            EquivalenceCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Lightbulb,
                value = "${s.equivalences.ledHours}",
                label = "hores de bombeta LED",
                tint = Color(0xFFFFB74D)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EquivalenceCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PhoneAndroid,
                value = "${s.equivalences.phoneCharges}",
                label = "càrregues de mòbil",
                tint = Color(0xFF9575CD)
            )
            EquivalenceCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Public,
                value = "${"%.0f".format(s.totalDistanceKm)} km",
                label = "distància total recorreguda",
                tint = Color(0xFF4DB6AC)
            )
        }

        // ----- Top 3 productos contaminantes -----
        if (s.topPolluters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Top productes amb més impacte",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            s.topPolluters.forEachIndexed { index, scan ->
                TopProductRow(rank = index + 1, scan = scan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------- Card de nivel ECO ----------

@Composable
private fun EcoLevelCard(
    level: EcoLevel,
    progress: Float,
    co2ToNext: Double,
    totalSaved: Double
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "level_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emoji grande del nivel
                Text(
                    text = level.emoji,
                    fontSize = 56.sp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nivell ${level.displayName}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = level.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Info de progreso
            val nextLevel = level.next()
            if (nextLevel != null) {
                Text(
                    text = "Estalvia ${"%.0f".format(co2ToNext)} g més de CO₂ per arribar a " +
                            "${nextLevel.emoji} ${nextLevel.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            } else {
                Text(
                    text = "Has arribat al màxim nivell. Ets una llegenda eco! 🏆",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ---------- Card grande genérica (CO₂ emès / ahorrat) ----------

@Composable
private fun BigStatCard(
    icon: ImageVector,
    title: String,
    mainValue: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(48.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
                Text(
                    text = mainValue,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ---------- Card pequeña de equivalencia ----------

@Composable
private fun EquivalenceCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

// ---------- Top product row ----------

@Composable
private fun TopProductRow(rank: Int, scan: ScanRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (rank) {
                    1 -> MaterialTheme.colorScheme.errorContainer
                    2 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.productName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${scan.originCountry} · ${"%.0f".format(scan.distanceKm)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = "${"%.0f".format(scan.co2Grams)}g",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}