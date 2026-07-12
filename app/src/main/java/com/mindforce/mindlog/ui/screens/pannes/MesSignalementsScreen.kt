package com.mindforce.mindlog.ui.screens.pannes

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
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.model.StatutPanne
import com.mindforce.mindlog.data.repository.PanneRepository

@Composable
fun MesSignalementsScreen(
    repository: PanneRepository,
    onBack: () -> Unit
) {
    val viewModel: MesSignalementsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MesSignalementsViewModel(repository) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    AndroidView(
        factory = { context ->
            val contextWrapper = ContextThemeWrapper(context, R.style.Theme_MindLog)
            val view = LayoutInflater.from(contextWrapper).inflate(R.layout.fragment_mes_signalements, null)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { onBack() }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSignalements)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = MesSignalementsAdapter()

            view
        },
        update = { view ->
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val errorBanner = view.findViewById<TextView>(R.id.errorBanner)
            val emptyStateText = view.findViewById<TextView>(R.id.emptyStateText)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSignalements)

            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            errorBanner.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
            errorBanner.text = state.errorMessage

            val showList = !state.isLoading && state.errorMessage == null && state.pannes.isNotEmpty()
            recyclerView.visibility = if (showList) View.VISIBLE else View.GONE
            emptyStateText.visibility = if (!state.isLoading && state.errorMessage == null && state.pannes.isEmpty()) View.VISIBLE else View.GONE

            (recyclerView.adapter as? MesSignalementsAdapter)?.submitList(state.pannes)
        }
    )
}

class MesSignalementsAdapter : RecyclerView.Adapter<MesSignalementsAdapter.ViewHolder>() {
    private var items = listOf<PanneResponse>()

    fun submitList(newItems: List<PanneResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mes_signalements, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(panne: PanneResponse) {
            itemView.findViewById<TextView>(R.id.materielName).text = "${panne.materielMarque ?: ""} ${panne.materielModele ?: ""}".trim()
            itemView.findViewById<TextView>(R.id.panneDescription).text = panne.descriptionPanne
            itemView.findViewById<TextView>(R.id.panneDate).text = panne.dateSignalement ?: "-"
            
            val statutBadge = itemView.findViewById<TextView>(R.id.statutBadge)
            val (label, colorRes) = when (panne.statutEtape) {
                StatutPanne.SIGNALE -> "Signalée" to android.R.color.holo_orange_dark
                StatutPanne.EN_REPARATION -> "En réparation" to android.R.color.holo_red_dark
                StatutPanne.RESOLUE -> "Résolue" to android.R.color.holo_green_dark
                StatutPanne.DECLASSE -> "Déclassé" to android.R.color.darker_gray
            }
            statutBadge.text = label
            statutBadge.setTextColor(itemView.context.getColor(colorRes))
            statutBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(itemView.context.getColor(colorRes)).withAlpha(40)
        }
    }
}
