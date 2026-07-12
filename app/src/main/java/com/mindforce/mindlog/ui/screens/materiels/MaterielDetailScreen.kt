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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.model.EtatMateriel
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.model.StatutPanne
import com.mindforce.mindlog.data.repository.MaterielRepository
import com.mindforce.mindlog.data.repository.PanneRepository

@Composable
fun MaterielDetailScreen(
    materielId: String,
    materielRepository: MaterielRepository,
    panneRepository: PanneRepository,
    onBack: () -> Unit,
    onSignalerPanne: (String) -> Unit
) {
    val viewModel: MaterielDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MaterielDetailViewModel(materielId, materielRepository, panneRepository) as T
        }
    })

    val state by viewModel.uiState.collectAsState()

    AndroidView(
        factory = { context ->
            val contextWrapper = ContextThemeWrapper(context, R.style.Theme_MindLog)
            val view = LayoutInflater.from(contextWrapper).inflate(R.layout.fragment_materiel_detail, null)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { onBack() }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = PanneAdapter()

            val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fabSignaler)
            fab.setOnClickListener { onSignalerPanne(materielId) }

            view
        },
        update = { view ->
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fabSignaler)
            
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            state.materiel?.let { materiel ->
                view.findViewById<Toolbar>(R.id.toolbar).title = "${materiel.marque} ${materiel.modele}"
                view.findViewById<TextView>(R.id.materielId).text = materiel.id
                
                // Rows info
                updateInfoRow(view.findViewById(R.id.rowType), "Type", materiel.typeMaterielNom ?: "-")
                updateInfoRow(view.findViewById(R.id.rowSerie), "N° série", materiel.numeroSerie ?: "-")
                updateInfoRow(view.findViewById(R.id.rowFournisseur), "Fournisseur", materiel.fournisseur ?: "-")
                updateInfoRow(view.findViewById(R.id.rowAcquisition), "Date d'acquisition", materiel.dateAcquisition ?: "-")

                val etatBadge = view.findViewById<TextView>(R.id.etatBadge)
                val (label, colorRes) = when (materiel.etatActuel) {
                    EtatMateriel.BON -> "Bon" to android.R.color.holo_green_dark
                    EtatMateriel.USAGE -> "Usagé" to android.R.color.holo_orange_dark
                    EtatMateriel.EN_PANNE -> "En panne" to android.R.color.holo_red_dark
                    EtatMateriel.DECLASSE -> "Déclassé" to android.R.color.darker_gray
                }
                etatBadge.text = label
                etatBadge.setTextColor(view.context.getColor(colorRes))
                etatBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(view.context.getColor(colorRes)).withAlpha(40)

                fab.visibility = if (materiel.etatActuel != EtatMateriel.DECLASSE) View.VISIBLE else View.GONE
            }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
            val emptyHistoryText = view.findViewById<TextView>(R.id.emptyHistoryText)
            
            if (state.historiquePannes.isEmpty() && !state.isLoading) {
                recyclerView.visibility = View.GONE
                emptyHistoryText.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyHistoryText.visibility = View.GONE
                (recyclerView.adapter as? PanneAdapter)?.submitList(state.historiquePannes)
            }
        }
    )
}

private fun updateInfoRow(row: View, label: String, value: String) {
    row.findViewById<TextView>(R.id.label).text = label
    row.findViewById<TextView>(R.id.value).text = value
}

class PanneAdapter : RecyclerView.Adapter<PanneAdapter.ViewHolder>() {
    private var items = listOf<PanneResponse>()

    fun submitList(newItems: List<PanneResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_panne, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(panne: PanneResponse) {
            itemView.findViewById<TextView>(R.id.panneDate).text = panne.dateSignalement ?: "-"
            itemView.findViewById<TextView>(R.id.panneDescription).text = panne.descriptionPanne
            
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
