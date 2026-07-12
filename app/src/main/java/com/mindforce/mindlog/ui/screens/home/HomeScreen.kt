package com.mindforce.mindlog.ui.screens.home

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.repository.DashboardRepository

@Composable
fun HomeScreen(
    sessionManager: SessionManager,
    dashboardRepository: DashboardRepository,
    onOpenMateriels: () -> Unit,
    onOpenMesSignalements: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(sessionManager, dashboardRepository) as T
        }
    })

    val state by viewModel.uiState.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // On utilise AndroidView pour charger le layout XML
    AndroidView(
        factory = { context ->
            // On s'assure d'utiliser le bon thème pour l'inflation
            val contextWrapper = ContextThemeWrapper(context, R.style.Theme_MindLog)
            val view = LayoutInflater.from(contextWrapper).inflate(R.layout.fragment_home, null)
            
            view.findViewById<View>(R.id.logoutButton).setOnClickListener {
                showLogoutConfirm = true
            }

            view.findViewById<View>(R.id.tileMateriels).setOnClickListener {
                onOpenMateriels()
            }

            view.findViewById<View>(R.id.tileSignalements).setOnClickListener {
                onOpenMesSignalements()
            }

            view
        },
        update = { view ->
            val userNameText = view.findViewById<TextView>(R.id.userNameText)
            val departementText = view.findViewById<TextView>(R.id.departementText)
            val totalMaterielsText = view.findViewById<TextView>(R.id.totalMaterielsText)
            val enPanneText = view.findViewById<TextView>(R.id.enPanneText)

            userNameText.text = state.userName.ifBlank { "Chef de Département" }
            
            if (!state.departementNom.isNullOrBlank()) {
                departementText.text = "Département : ${state.departementNom}"
                departementText.visibility = View.VISIBLE
            } else {
                departementText.visibility = View.GONE
            }

            // Stats
            totalMaterielsText.text = state.stats?.totalMateriels?.toString() ?: "--"
            enPanneText.text = state.stats?.materielsEnPanne?.toString() ?: "--"
        }
    )

    if (showLogoutConfirm) {
        // On peut garder l'AlertDialog de Compose ou utiliser un AlertDialog classique
        AlertDialog.Builder(androidx.compose.ui.platform.LocalContext.current)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setPositiveButton("Se déconnecter") { _, _ ->
                showLogoutConfirm = false
                onLogout()
            }
            .setNegativeButton("Annuler") { _, _ ->
                showLogoutConfirm = false
            }
            .setOnDismissListener { showLogoutConfirm = false }
            .show()
    }
}
