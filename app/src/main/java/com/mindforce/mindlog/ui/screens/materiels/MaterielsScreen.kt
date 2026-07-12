package com.mindforce.mindlog.ui.screens.materiels

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.EtatMateriel
import com.mindforce.mindlog.data.model.MaterielResponse
import com.mindforce.mindlog.data.repository.MaterielRepository

@Composable
fun MaterielsScreen(
    repository: MaterielRepository,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onMaterielClick: (String) -> Unit
) {
    val viewModel: MaterielsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MaterielsViewModel(repository, sessionManager) as T
        }
    })

    val state by viewModel.uiState.collectAsState()

    AndroidView(
        factory = { context ->
            val contextWrapper = ContextThemeWrapper(context, R.style.Theme_MindLog)
            val view = LayoutInflater.from(contextWrapper).inflate(R.layout.fragment_materiels, null)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { onBack() }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMateriels)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = MaterielAdapter(onMaterielClick)

            view
        },
        update = { view ->
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val errorBanner = view.findViewById<TextView>(R.id.errorBanner)
            val emptyStateText = view.findViewById<TextView>(R.id.emptyStateText)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMateriels)

            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            errorBanner.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
            errorBanner.text = state.errorMessage

            val showList = !state.isLoading && state.errorMessage == null && state.materiels.isNotEmpty()
            recyclerView.visibility = if (showList) View.VISIBLE else View.GONE
            emptyStateText.visibility = if (!state.isLoading && state.errorMessage == null && state.materiels.isEmpty()) View.VISIBLE else View.GONE

            (recyclerView.adapter as? MaterielAdapter)?.submitList(state.materiels)
        }
    )
}

class MaterielAdapter(private val onClick: (String) -> Unit) : RecyclerView.Adapter<MaterielAdapter.ViewHolder>() {
    private var items = listOf<MaterielResponse>()

    fun submitList(newItems: List<MaterielResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_materiel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(materiel: MaterielResponse, onClick: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.materielTitle).text = "${materiel.marque} ${materiel.modele}"
            itemView.findViewById<TextView>(R.id.materielType).text = materiel.typeMaterielNom ?: ""
            itemView.findViewById<TextView>(R.id.materielId).text = materiel.id
            
            val etatBadge = itemView.findViewById<TextView>(R.id.etatBadge)
            val (label, colorRes) = when (materiel.etatActuel) {
                EtatMateriel.BON -> "Bon" to android.R.color.holo_green_dark
                EtatMateriel.USAGE -> "Usagé" to android.R.color.holo_orange_dark
                EtatMateriel.EN_PANNE -> "En panne" to android.R.color.holo_red_dark
                EtatMateriel.DECLASSE -> "Déclassé" to android.R.color.darker_gray
            }
            etatBadge.text = label
            etatBadge.setTextColor(itemView.context.getColor(colorRes))
            etatBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(itemView.context.getColor(colorRes)).withAlpha(40)

            itemView.setOnClickListener { onClick(materiel.id) }
        }
    }
}
