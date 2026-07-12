package com.mindforce.mindlog.ui.screens.pannes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.PanneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MesSignalementsUiState(
    val isLoading: Boolean = true,
    val pannes: List<PanneResponse> = emptyList(),
    val errorMessage: String? = null
)

class MesSignalementsViewModel(private val repository: PanneRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MesSignalementsUiState())
    val uiState: StateFlow<MesSignalementsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getMesSignalements()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, pannes = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
