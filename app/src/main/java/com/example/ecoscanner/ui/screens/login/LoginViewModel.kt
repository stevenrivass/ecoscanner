package com.example.ecoscanner.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.repository.AuthRepository
import com.example.ecoscanner.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val user: User) : LoginUiState
    // Cuenta creada pero email no verificado todavía
    data class PendingVerification(val email: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
    data object VerificationEmailResent : LoginUiState
}

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateBasic(email, password)) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.login(email.trim(), password)
                .onSuccess { user ->
                    // Comprobamos si el email está verificado
                    if (repo.isEmailVerified()) {
                        _state.value = LoginUiState.Success(user)
                    } else {
                        // Está registrado pero no ha verificado el email
                        _state.value = LoginUiState.PendingVerification(user.email)
                        repo.logout()
                    }
                }
                .onFailure { _state.value = LoginUiState.Error(friendly(it.message)) }
        }
    }

    fun register(email: String, password: String) {
        if (!validateEmail(email)) return
        if (!validateStrongPassword(password)) return

        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.register(email.trim(), password)
                .onSuccess { user ->
                    // Recién registrado → email aún no verificado
                    _state.value = LoginUiState.PendingVerification(user.email)
                    repo.logout()  // forzamos logout para que verifique antes
                }
                .onFailure { _state.value = LoginUiState.Error(friendly(it.message)) }
        }
    }

    // Comprobar de nuevo si ya verificó (después de hacer click en el correo)
    fun checkEmailVerified(email: String, password: String) {
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.login(email.trim(), password)
                .onSuccess { user ->
                    repo.reloadUser()
                        .onSuccess { verified ->
                            if (verified) {
                                _state.value = LoginUiState.Success(user)
                            } else {
                                _state.value = LoginUiState.PendingVerification(user.email)
                                repo.logout()
                            }
                        }
                        .onFailure {
                            _state.value = LoginUiState.Error("Error comprovant el correu")
                        }
                }
                .onFailure { _state.value = LoginUiState.Error(friendly(it.message)) }
        }
    }

    fun resendVerificationEmail(email: String, password: String) {
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.login(email.trim(), password)
                .onSuccess {
                    repo.resendVerificationEmail()
                        .onSuccess {
                            _state.value = LoginUiState.VerificationEmailResent
                            // Tras 2 segundos volvemos al estado pendiente
                            kotlinx.coroutines.delay(2000)
                            _state.value = LoginUiState.PendingVerification(email)
                            repo.logout()
                        }
                        .onFailure {
                            _state.value = LoginUiState.Error("No s'ha pogut reenviar")
                        }
                }
                .onFailure { _state.value = LoginUiState.Error(friendly(it.message)) }
        }
    }

    fun resetState() {
        _state.value = LoginUiState.Idle
    }

    // Validación básica para login (no exige fortaleza, ya está validada al registrar)
    private fun validateBasic(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginUiState.Error("Omple email i contrasenya.")
            return false
        }
        return true
    }

    // Valida formato de email
    private fun validateEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            _state.value = LoginUiState.Error("Introdueix un correu electrònic.")
            return false
        }
        // Patrón de Android para validar emails
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            _state.value = LoginUiState.Error("El format del correu no és vàlid.")
            return false
        }
        return true
    }

    // Validación de contraseña FUERTE (solo al registrar)
    // Reglas: mínimo 8 caracteres, una mayúscula, una minúscula, un número
    private fun validateStrongPassword(password: String): Boolean {
        when {
            password.isBlank() -> {
                _state.value = LoginUiState.Error("Introdueix una contrasenya.")
                return false
            }
            password.length < 8 -> {
                _state.value = LoginUiState.Error(
                    "La contrasenya ha de tenir mínim 8 caràcters."
                )
                return false
            }
            !password.any { it.isUpperCase() } -> {
                _state.value = LoginUiState.Error(
                    "La contrasenya ha de tenir almenys una majúscula."
                )
                return false
            }
            !password.any { it.isLowerCase() } -> {
                _state.value = LoginUiState.Error(
                    "La contrasenya ha de tenir almenys una minúscula."
                )
                return false
            }
            !password.any { it.isDigit() } -> {
                _state.value = LoginUiState.Error(
                    "La contrasenya ha de tenir almenys un número."
                )
                return false
            }
            isCommonPassword(password) -> {
                _state.value = LoginUiState.Error(
                    "Aquesta contrasenya és massa comuna. Tria'n una de més segura."
                )
                return false
            }
        }
        return true
    }

    // Lista negra de las contraseñas más comunes
    private fun isCommonPassword(password: String): Boolean {
        val common = setOf(
            "12345678", "123456789", "1234567890",
            "password", "Password1", "Password123",
            "qwerty123", "Qwerty123",
            "abc12345", "Abc12345",
            "11111111", "00000000",
            "admin123", "Admin123"
        )
        return password in common
    }

    private fun friendly(msg: String?): String = when {
        msg == null -> "Error desconegut"
        msg.contains("password", true) && msg.contains("invalid", true) ->
            "Contrasenya incorrecta."
        msg.contains("no user record", true) ||
                msg.contains("user not found", true) ->
            "Aquest usuari no existeix."
        msg.contains("already in use", true) ->
            "Aquest email ja està registrat."
        msg.contains("badly formatted", true) ->
            "Format d'email no vàlid."
        msg.contains("network", true) ->
            "Sense connexió a internet."
        msg.contains("weak", true) ->
            "La contrasenya és massa feble."
        else -> msg
    }
}