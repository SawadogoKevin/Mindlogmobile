package com.mindforce.mindlog.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.DashboardStats
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.model.StatutPanne
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.DashboardRepository
import com.mindforce.mindlog.data.repository.PanneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val departementNom: String? = null,
    val stats: DashboardStats? = null,
    val untreadedPannes: List<PanneResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository,
    private val panneRepository: PanneRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadSessionInfo()
        refresh()
    }

    private fun loadSessionInfo() {
        viewModelScope.launch {
            val name = sessionManager.getDisplayName()
            val dept = sessionManager.getDepartementNom()
            _uiState.value = _uiState.value.copy(userName = name, departementNom = dept)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Stats
            val statsResult = dashboardRepository.getStats()
            if (statsResult is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(stats = statsResult.data)
            }
            
            // Untreated Pannes (SIGNALE only)
            val pannesResult = panneRepository.getMesSignalements()
            if (pannesResult is ApiResult.Success) {
                val signalePannes = pannesResult.data
                    .filter { it.statutEtape == StatutPanne.SIGNALE }
                    .take(2)
                _uiState.value = _uiState.value.copy(untreadedPannes = signalePannes)
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
