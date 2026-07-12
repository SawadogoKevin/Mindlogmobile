package com.mindforce.mindlog.data.model

enum class EtatMateriel {
    BON, USAGE, DECLASSE, EN_PANNE
}

data class MaterielResponse(
    val id: String,
    val marque: String,
    val modele: String,
    val numeroSerie: String?,
    val dateAcquisition: String?,
    val dateDebutUtilisation: String?,
    val fournisseur: String?,
    val etatActuel: EtatMateriel,
    val typeMaterielId: Long?,
    val typeMaterielNom: String?,
    val dateCreation: String?
)
