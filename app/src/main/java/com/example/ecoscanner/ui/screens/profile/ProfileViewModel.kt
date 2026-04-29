package com.example.ecoscanner.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.repository.AuthRepository
import com.example.ecoscanner.data.repository.ThemeMode
import com.example.ecoscanner.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val nameInput: String = "",
    val nameLoaded: Boolean = false,
    val savingName: Boolean = false,
    val nameSaved: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios creados internamente (evita problemas con ViewModelFactory)
    private val authRepo = AuthRepository()
    private val prefsRepo = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = prefsRepo.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val current = authRepo.currentUser()
        _uiState.value = _uiState.value.copy(email = current?.email ?: "")

        viewModelScope.launch {
            val name = authRepo.getDisplayName() ?: ""
            _uiState.value = _uiState.value.copy(
                displayName = name,
                nameInput = name,
                nameLoaded = true
            )
        }
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(
            nameInput = newName,
            nameSaved = false
        )
    }

    fun saveName() {
        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El nom no pot estar buit.")
            return
        }
        _uiState.value = _uiState.value.copy(savingName = true, errorMessage = null)
        viewModelScope.launch {
            authRepo.setDisplayName(name)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        displayName = name,
                        savingName = false,
                        nameSaved = true
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        savingName = false,
                        errorMessage = it.localizedMessage ?: "Error en guardar."
                    )
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsRepo.setThemeMode(mode)
        }
    }

    fun logout() {
        authRepo.logout()
    }
}