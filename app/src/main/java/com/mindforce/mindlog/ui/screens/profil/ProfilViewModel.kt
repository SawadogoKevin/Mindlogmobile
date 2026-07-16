package com.mindforce.mindlog.ui.screens.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.UpdatePasswordRequest
import com.mindforce.mindlog.data.model.UpdateProfileRequest
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.DashboardRepository
import com.mindforce.mindlog.data.repository.UserRepository
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
    val matricule: String = "",
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Password change fields
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isChangingPassword: Boolean = false,
    val passwordErrorMessage: String? = null,
    val passwordSuccessMessage: String? = null
)

class ProfilViewModel(
    private val sessionManager: SessionManager,
    private val dashboardRepository: DashboardRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState: StateFlow<ProfilUiState> = _uiState

    init {
        loadProfil()
    }

    private fun loadProfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Try to load from API first
            val result = userRepository.getMyProfile()
            when (result) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        nom = user.nom,
                        prenom = user.prenom,
                        email = user.email,
                        telephone = user.telephone ?: "",
                        role = user.role,
                        matricule = user.matricule ?: "",
                        isLoading = false
                    )
                    // Update session with latest data
                    sessionManager.saveSession(
                        token = sessionManager.getToken() ?: "",
                        email = user.email,
                        role = user.role,
                        nom = user.nom,
                        prenom = user.prenom,
                        userId = user.id,
                        departementId = sessionManager.getDepartementId(),
                        departementNom = sessionManager.getDepartementNom()
                    )
                }
                is ApiResult.Error -> {
                    // Fallback to session data if API fails
                    val prenom = sessionManager.getPrenom() ?: ""
                    val nom = sessionManager.getNom() ?: ""
                    val email = sessionManager.getEmail() ?: ""
                    val role = sessionManager.getRole() ?: ""
                    var departement = sessionManager.getDepartementNom() ?: ""
                    
                    // Si le département est vide, on tente de le récupérer via les stats dashboard
                    if (departement.isBlank()) {
                        val userId = sessionManager.getUserId()
                        if (userId != null) {
                            val statsResult = dashboardRepository.getStats(userId)
                            if (statsResult is ApiResult.Success) {
                                departement = statsResult.data.nomDepartement ?: ""
                                if (departement.isNotBlank()) {
                                    sessionManager.saveDepartementNom(departement)
                                }
                            }
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        nom = nom,
                        prenom = prenom,
                        email = email,
                        role = role,
                        departement = departement,
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditing = !_uiState.value.isEditing,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateNom(nom: String) {
        _uiState.value = _uiState.value.copy(nom = nom)
    }

    fun updatePrenom(prenom: String) {
        _uiState.value = _uiState.value.copy(prenom = prenom)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateTelephone(telephone: String) {
        _uiState.value = _uiState.value.copy(telephone = telephone)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
            
            val request = UpdateProfileRequest(
                nom = _uiState.value.nom,
                prenom = _uiState.value.prenom,
                email = _uiState.value.email,
                telephone = _uiState.value.telephone
            )
            
            val result = userRepository.updateMyProfile(request)
            when (result) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(
                        nom = user.nom,
                        prenom = user.prenom,
                        email = user.email,
                        telephone = user.telephone ?: "",
                        isSaving = false,
                        isEditing = false,
                        successMessage = "Profil mis à jour avec succès"
                    )
                    // Update session with latest data
                    sessionManager.saveSession(
                        token = sessionManager.getToken() ?: "",
                        email = user.email,
                        role = user.role,
                        nom = user.nom,
                        prenom = user.prenom,
                        userId = user.id,
                        departementId = sessionManager.getDepartementId(),
                        departementNom = sessionManager.getDepartementNom()
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updateCurrentPassword(password: String) {
        _uiState.value = _uiState.value.copy(currentPassword = password, passwordErrorMessage = null)
    }

    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password, passwordErrorMessage = null)
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password, passwordErrorMessage = null)
    }

    fun changePassword() {
        val currentPassword = _uiState.value.currentPassword
        val newPassword = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword

        // Validation
        when {
            currentPassword.isBlank() -> {
                _uiState.value = _uiState.value.copy(passwordErrorMessage = "Veuillez entrer votre mot de passe actuel")
                return
            }
            newPassword.isBlank() -> {
                _uiState.value = _uiState.value.copy(passwordErrorMessage = "Veuillez entrer votre nouveau mot de passe")
                return
            }
            confirmPassword.isBlank() -> {
                _uiState.value = _uiState.value.copy(passwordErrorMessage = "Veuillez confirmer votre nouveau mot de passe")
                return
            }
            newPassword != confirmPassword -> {
                _uiState.value = _uiState.value.copy(passwordErrorMessage = "Les nouveaux mots de passe ne correspondent pas")
                return
            }
            newPassword.length < 6 -> {
                _uiState.value = _uiState.value.copy(passwordErrorMessage = "Le nouveau mot de passe doit contenir au moins 6 caractères")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChangingPassword = true, passwordErrorMessage = null, passwordSuccessMessage = null)
            
            val request = UpdatePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
            
            val result = userRepository.updateMyPassword(request)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        passwordSuccessMessage = "Mot de passe modifié avec succès"
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        passwordErrorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null,
            passwordErrorMessage = null,
            passwordSuccessMessage = null
        )
    }
}
