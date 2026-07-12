package com.mindforce.mindlog.ui.screens.materiels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.MaterielResponse
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.MaterielRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MaterielsUiState(
    val isLoading: Boolean = true,
    val materiels: List<MaterielResponse> = emptyList(),
    val errorMessage: String? = null
)

class MaterielsViewModel(
    private val repository: MaterielRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterielsUiState())
    val uiState: StateFlow<MaterielsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val deptId = sessionManager.getDepartementId()
            if (deptId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "ID du département non trouvé")
                return@launch
            }
            when (val result = repository.getMesMateriels(deptId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, materiels = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
