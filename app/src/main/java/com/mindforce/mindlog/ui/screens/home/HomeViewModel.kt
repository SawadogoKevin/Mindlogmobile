package com.mindforce.mindlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.DashboardStats
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val departementNom: String? = null,
    val stats: DashboardStats? = null,
    val isLoadingStats: Boolean = false,
    val errorStats: String? = null
)

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadSessionInfo()
        refreshStats()
    }

    private fun loadSessionInfo() {
        viewModelScope.launch {
            val name = sessionManager.getDisplayName()
            val dept = sessionManager.getDepartementNom()
            _uiState.value = _uiState.value.copy(userName = name, departementNom = dept)
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStats = true, errorStats = null)
            when (val result = dashboardRepository.getStats()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoadingStats = false, stats = result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingStats = false, errorStats = result.message)
                }
            }
        }
    }
}
