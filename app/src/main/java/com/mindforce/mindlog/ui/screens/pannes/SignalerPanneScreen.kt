package com.mindforce.mindlog.ui.screens.pannes

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.load
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.TypePanne
import com.mindforce.mindlog.data.repository.PanneRepository
import com.mindforce.mindlog.util.PhotoFileUtil

@Composable
fun SignalerPanneScreen(
    materielId: String,
    panneRepository: PanneRepository,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SignalerPanneViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SignalerPanneViewModel(materielId, panneRepository, sessionManager) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    var pendingCameraFile by remember { mutableStateOf<java.io.File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingCameraFile
        if (success && file != null) {
            val uri = PhotoFileUtil.uriForFile(context, file)
            viewModel.onPhotoReady(uri, file)
        } else {
            Toast.makeText(context, "Photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val file = PhotoFileUtil.copyContentUriToFile(context, uri)
            if (file != null) {
                viewModel.onPhotoReady(uri, file)
            } else {
                Toast.makeText(context, "Impossible de charger cette image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.fragment_signaler_panne, null)
            
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { onBack() }

            val descriptionInput = view.findViewById<TextInputEditText>(R.id.descriptionInput)
            descriptionInput.doAfterTextChanged { viewModel.onDescriptionChange(it.toString()) }

            val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)
            toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val type = if (checkedId == R.id.buttonReparable) TypePanne.REPARABLE else TypePanne.NON_REPARABLE
                    viewModel.onTypeChange(type)
                }
            }

            view.findViewById<Button>(R.id.cameraButton).setOnClickListener {
                val file = PhotoFileUtil.createCameraOutputFile(context)
                pendingCameraFile = file
                val uri = PhotoFileUtil.uriForFile(context, file)
                cameraLauncher.launch(uri)
            }

            view.findViewById<Button>(R.id.galleryButton).setOnClickListener {
                galleryLauncher.launch("image/*")
            }

            view.findViewById<ImageButton>(R.id.removePhotoButton).setOnClickListener {
                viewModel.clearPhoto()
            }

            view.findViewById<Button>(R.id.submitButton).setOnClickListener {
                viewModel.submit()
            }

            view
        },
        update = { view ->
            view.findViewById<TextView>(R.id.materielIdText).text = state.materielId
            
            val errorBanner = view.findViewById<TextView>(R.id.errorBanner)
            errorBanner.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
            errorBanner.text = state.errorMessage

            val photoContainer = view.findViewById<FrameLayout>(R.id.photoContainer)
            val photoPreview = view.findViewById<ImageView>(R.id.photoPreview)
            if (state.photoUri != null) {
                photoContainer.visibility = View.VISIBLE
                photoPreview.load(state.photoUri)
            } else {
                photoContainer.visibility = View.GONE
            }

            val submitButton = view.findViewById<Button>(R.id.submitButton)
            submitButton.isEnabled = !state.isSubmitting
            submitButton.text = if (state.isSubmitting) "Envoi..." else "Signaler la panne"

            // Update toggle state if changed from outside (unlikely here but good practice)
            val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)
            val expectedId = if (state.typePanne == TypePanne.REPARABLE) R.id.buttonReparable else R.id.buttonNonReparable
            if (toggleGroup.checkedButtonId != expectedId) {
                toggleGroup.check(expectedId)
            }
        }
    )
}
