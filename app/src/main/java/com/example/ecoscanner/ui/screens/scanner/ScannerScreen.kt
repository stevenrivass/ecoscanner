package com.example.ecoscanner.ui.screens.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ─── ScannerScreen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onScanClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Eco,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(24.dp)
                        )
                        Text(
                            text       = "EcoScanner",
                            style      = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onStatisticsClick) {
                        Icon(
                            imageVector        = Icons.Default.BarChart,
                            contentDescription = "Ver estadísticas",
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text  = "Escanear producto",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text  = "Obtén la huella de carbono al instante",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // ── Viewfinder simulado de la cámara ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)           // Cuadrado perfecto
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                // Overlay de esquinas tipo viewfinder
                CameraViewfinderOverlay()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.CameraAlt,
                        contentDescription = "Cámara",
                        tint               = Color.White.copy(alpha = 0.7f),
                        modifier           = Modifier.size(72.dp)
                    )
                    Text(
                        text  = "Vista previa de cámara",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // ── Instrucción ───────────────────────────────────────────────
            Text(
                text      = "Apunta al código de barras del producto",
                style     = MaterialTheme.typography.bodyLarge.copy(
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── Botón Simular Escaneo ─────────────────────────────────────
            Button(
                onClick  = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text  = "Simular Escaneo",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Overlay decorativo con esquinas tipo visor ────────────────────────────

@Composable
private fun CameraViewfinderOverlay() {
    val cornerColor = MaterialTheme.colorScheme.primary
    val cornerSize  = 40.dp
    val strokeWidth = 4.dp

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {

        // Esquina superior izquierda
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
                .border(
                    width = strokeWidth,
                    color = cornerColor,
                    shape = RoundedCornerShape(topStart = 12.dp)
                )
        )
        // Esquina superior derecha
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
                .border(
                    width = strokeWidth,
                    color = cornerColor,
                    shape = RoundedCornerShape(topEnd = 12.dp)
                )
        )
        // Esquina inferior izquierda
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
                .border(
                    width = strokeWidth,
                    color = cornerColor,
                    shape = RoundedCornerShape(bottomStart = 12.dp)
                )
        )
        // Esquina inferior derecha
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
                .border(
                    width = strokeWidth,
                    color = cornerColor,
                    shape = RoundedCornerShape(bottomEnd = 12.dp)
                )
        )
    }
}