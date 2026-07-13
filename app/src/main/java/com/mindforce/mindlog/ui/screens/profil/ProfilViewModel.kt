package com.mindforce.mindlog.ui.screens.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfilUiState(
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val telephone: String = "",
    val fonction: String = "",
    val departement: String = "",
    val role: String = "",
    val isLoading: Boolean = false
)

class ProfilViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState: StateFlow<ProfilUiState> = _uiState

    init {
        loadProfil()
    }

    private fun loadProfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prenom = sessionManager.getPrenom() ?: ""
            val nom = sessionManager.getNom() ?: ""
            val email = sessionManager.getEmail() ?: ""
            val role = sessionManager.getRole() ?: ""
            val departement = sessionManager.getDepartementNom() ?: ""
            
            _uiState.value = _uiState.value.copy(
                nom = nom,
                prenom = prenom,
                email = email,
                role = role,
                departement = departement,
                isLoading = false
            )
        }
    }
}
