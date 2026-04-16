package com.example.ecoscanner.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── Mock Data ────────────────────────────────────────────────────────────

data class ProductHistory(
    val name: String,
    val country: String,
    val flag: String,
    val co2Grams: Int,
    val distance: String,
    val icon: ImageVector,
    val co2Level: Co2Level            // Para el color del badge
)

enum class Co2Level { LOW, MEDIUM, HIGH }

private val mockHistory = listOf(
    ProductHistory(
        name     = "Manzana Golden",
        country  = "Italia",
        flag     = "🇮🇹",
        co2Grams = 164,
        distance = "1.200 km",
        icon     = Icons.Outlined.Eco,
        co2Level = Co2Level.MEDIUM
    ),
    ProductHistory(
        name     = "Leche Entera Bio",
        country  = "España",
        flag     = "🇪🇸",
        co2Grams = 22,
        distance = "85 km",
        icon     = Icons.Outlined.WaterDrop,
        co2Level = Co2Level.LOW
    ),
    ProductHistory(
        name     = "Plátano de Canarias",
        country  = "España",
        flag     = "🇪🇸",
        co2Grams = 18,
        distance = "65 km",
        icon     = Icons.Outlined.Spa,
        co2Level = Co2Level.LOW
    ),
    ProductHistory(
        name     = "Salmón Noruego",
        country  = "Noruega",
        flag     = "🇳🇴",
        co2Grams = 246,
        distance = "2.700 km",
        icon     = Icons.Outlined.SetMeal,
        co2Level = Co2Level.HIGH
    )
)

// ─── StatisticsScreen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit
) {
    val totalCo2 = mockHistory.sumOf { it.co2Grams }     // 450 g

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Mi historial",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {

            // ── Cabecera de resumen ───────────────────────────────────────
            item {
                SummaryCard(totalCo2 = totalCo2)
            }

            // ── Título de sección ─────────────────────────────────────────
            item {
                Row(
                    modifier             = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Productos escaneados",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text  = "${mockHistory.size} items",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // ── Lista de productos ────────────────────────────────────────
            items(mockHistory) { product ->
                ProductHistoryCard(product = product)
            }

            // ── Footer informativo ────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = "💡 Un adulto produce ~2.500g CO₂ al día en alimentación.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── Card de resumen total ─────────────────────────────────────────────────

@Composable
private fun SummaryCard(totalCo2: Int) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text  = "Este mes has emitido",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                )
                Text(
                    text  = "${totalCo2}g de CO₂",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text  = "en ${mockHistory.size} productos escaneados",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(36.dp)
                )
            }
        }
    }
}

// ── Card de cada producto del historial ───────────────────────────────────

@Composable
private fun ProductHistoryCard(product: ProductHistory) {
    val (badgeColor, badgeText) = when (product.co2Level) {
        Co2Level.LOW    -> Pair(MaterialTheme.colorScheme.tertiary,        "Bajo")
        Co2Level.MEDIUM -> Pair(MaterialTheme.colorScheme.secondary,       "Medio")
        Co2Level.HIGH   -> Pair(MaterialTheme.colorScheme.error,           "Alto")
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono del producto
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = product.icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.secondary,
                    modifier           = Modifier.size(26.dp)
                )
            }

            // Datos del producto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text  = product.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text  = "${product.flag} ${product.country}  ·  ${product.distance}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // CO2 + badge de nivel
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = "${product.co2Grams}g",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text     = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style    = MaterialTheme.typography.labelSmall.copy(
                            color      = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
