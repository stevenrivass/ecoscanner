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
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.login(email.trim(), password)
                .onSuccess { _state.value = LoginUiState.Success(it) }
                .onFailure { _state.value = LoginUiState.Error(friendly(it.message)) }
        }
    }

    fun register(email: String, password: String) {
        if (!validate(email, password)) return
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            repo.register(email.trim(), password)
                .onSuccess { _state.value = LoginUiState.Success(it) }
                .onFailure { _state.value = LoginUiState.Error(friendly(it.message)) }
        }
    }

    fun resetState() {
        _state.value = LoginUiState.Idle
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginUiState.Error("Rellena email y contraseña.")
            return false
        }
        if (password.length < 6) {
            _state.value = LoginUiState.Error("La contraseña debe tener 6 caracteres o más.")
            return false
        }
        return true
    }

    private fun friendly(msg: String?): String = when {
        msg == null -> "Error desconocido"
        msg.contains("password", true) && msg.contains("invalid", true) ->
            "Contraseña incorrecta."
        msg.contains("no user record", true) ||
                msg.contains("user not found", true) ->
            "Este usuario no existe."
        msg.contains("already in use", true) ->
            "Este email ya está registrado."
        msg.contains("badly formatted", true) ->
            "Email no válido."
        msg.contains("network", true) ->
            "Sin conexión a internet."
        else -> msg
    }
}