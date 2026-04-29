package com.example.ecoscanner.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoscanner.data.repository.FirestoreScanRepository
import com.example.ecoscanner.model.ScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Success(val scans: List<ScanRecord>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

class HistoryViewModel(
    private val repo: FirestoreScanRepository = FirestoreScanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = HistoryUiState.Loading
        viewModelScope.launch {
            repo.getAllScans()
                .onSuccess { list ->
                    _state.value = if (list.isEmpty()) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Success(list)
                    }
                }
                .onFailure {
                    _state.value = HistoryUiState.Error(
                        it.localizedMessage ?: "Error desconegut"
                    )
                }
        }
    }
}