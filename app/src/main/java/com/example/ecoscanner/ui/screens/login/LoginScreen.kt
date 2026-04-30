package com.example.ecoscanner.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ecoscanner.R
import com.example.ecoscanner.ui.navigation.Routes

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoading = state is LoginUiState.Loading

    // Cuando login es exitoso → al MainScreen
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val s = state) {
            is LoginUiState.PendingVerification -> {
                EmailVerificationScreen(
                    email = s.email,
                    isLoading = isLoading,
                    onCheckVerified = {
                        viewModel.checkEmailVerified(email, password)
                    },
                    onResend = {
                        viewModel.resendVerificationEmail(email, password)
                    },
                    onBackToLogin = {
                        viewModel.resetState()
                    }
                )
            }
            is LoginUiState.VerificationEmailResent -> {
                ResentConfirmationScreen()
            }
            else -> {
                LoginForm(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    state = state,
                    isLoading = isLoading,
                    onLogin = { viewModel.login(email, password) },
                    onRegister = { viewModel.register(email, password) }
                )
            }
        }
    }
}

// ---------- Pantalla principal de login ----------

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    state: LoginUiState,
    isLoading: Boolean,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(R.drawable.icon),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Conoce el origen de tus productos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        // Indicador de fortaleza de contraseña (solo si hay algo escrito)
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthIndicator(password = password)
        }

        // Mensaje de error (si lo hay)
        if (state is LoginUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón principal: Acceder
        Button(
            onClick = onLogin,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Acceder",
                    style = MaterialTheme.typography.titleMedium
                        .copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón secundario: Crear cuenta
        OutlinedButton(
            onClick = onRegister,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ---------- Indicador visual de fortaleza ----------

@Composable
private fun PasswordStrengthIndicator(password: String) {
    val checks = listOf(
        "Mínim 8 caràcters" to (password.length >= 8),
        "Una majúscula" to password.any { it.isUpperCase() },
        "Una minúscula" to password.any { it.isLowerCase() },
        "Un número" to password.any { it.isDigit() }
    )

    val passedCount = checks.count { it.second }

    val strengthColor = when (passedCount) {
        4 -> Color(0xFF4CAF50)   // verde — fuerte
        3 -> Color(0xFFFFA726)   // naranja — media
        else -> Color(0xFFE57373) // rojo — débil
    }

    val strengthLabel = when (passedCount) {
        4 -> "Forta ✓"
        3 -> "Mitjana"
        2 -> "Feble"
        else -> "Molt feble"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Seguretat:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = strengthLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = strengthColor
                )
            }
            // Barra visual de progreso
            LinearProgressIndicator(
                progress = { passedCount / 4f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = strengthColor,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Checklist de requisitos
            Spacer(modifier = Modifier.height(4.dp))
            checks.forEach { (label, passed) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (passed) "✓" else "○",
                        color = if (passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (passed) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---------- Pantalla "verifica tu email" ----------

@Composable
private fun EmailVerificationScreen(
    email: String,
    isLoading: Boolean,
    onCheckVerified: () -> Unit,
    onResend: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Email,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Verifica el teu correu",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "T'hem enviat un correu de verificació a:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = email,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fes click a l'enllaç del correu per activar el teu compte. " +
                    "Pot trigar 1-2 minuts a arribar. Revisa també la carpeta de spam.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCheckVerified,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Ja he verificat el correu",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onResend,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Reenviar correu de verificació")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Tornar al login")
        }
    }
}

// ---------- Pantalla de "correu reenviat" ----------

@Composable
private fun ResentConfirmationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Correu reenviat",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Revisa la teva safata d'entrada",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}