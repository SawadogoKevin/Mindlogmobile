package com.mindforce.mindlog.ui.screens.pannes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.PanneRequest
import com.mindforce.mindlog.data.model.TypePanne
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.PanneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SignalerPanneUiState(
    val materielId: String = "",
    val description: String = "",
    val typePanne: TypePanne = TypePanne.REPARABLE,
    val photoUri: Uri? = null,
    val photoFile: File? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class SignalerPanneViewModel(
    materielId: String,
    private val panneRepository: PanneRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignalerPanneUiState(materielId = materielId))
    val uiState: StateFlow<SignalerPanneUiState> = _uiState

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, errorMessage = null)
    }

    fun onTypeChange(value: TypePanne) {
        _uiState.value = _uiState.value.copy(typePanne = value)
    }

    /** Appelé après capture caméra ou sélection galerie, une fois le fichier local prêt */
    fun onPhotoReady(uri: Uri, file: File) {
        _uiState.value = _uiState.value.copy(photoUri = uri, photoFile = file, errorMessage = null)
    }

    fun clearPhoto() {
        _uiState.value = _uiState.value.copy(photoUri = null, photoFile = null)
    }

    fun submit() {
        val state = _uiState.value

        if (state.description.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez décrire la panne")
            return
        }
        if (state.photoFile == null) {
            _uiState.value = state.copy(errorMessage = "Une photo justificative est obligatoire (prenez une photo ou choisissez-en une dans la galerie)")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = "Session invalide, veuillez vous reconnecter")
                return@launch
            }

            val request = PanneRequest(
                descriptionPanne = state.description.trim(),
                typePanne = state.typePanne,
                materielId = state.materielId,
                signaleParId = userId
            )

            when (val result = panneRepository.signaler(request, state.photoFile)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isSubmitting = false, success = true)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
            }
        }
    }
}
