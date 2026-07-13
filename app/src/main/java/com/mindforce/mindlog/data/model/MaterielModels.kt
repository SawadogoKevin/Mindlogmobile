package com.mindforce.mindlog.data.model

import com.google.gson.annotations.SerializedName

enum class EtatMateriel {
    @SerializedName("BON") BON,
    @SerializedName("USAGE") USAGE,
    @SerializedName("DECLASSE") DECLASSE,
    @SerializedName("EN_PANNE") EN_PANNE
}

data class MaterielResponse(
    val id: String,
    val marque: String,
    val modele: String,
    val numeroSerie: String?,
    val dateAcquisition: String?,
    val dateDebutUtilisation: String?,
    val fournisseur: String?,
    @SerializedName("etat_actuel")
    val etatActuel: EtatMateriel?,
    val typeMaterielId: Long?,
    val typeMaterielNom: String?,
    val dateCreation: String?
)

data class AffectationMaterielResponse(
    val id: Long,
    val materielId: String,
    val materielMarque: String?,
    val materielModele: String?,
    @SerializedName("etat_actuel")
    val materielEtatActuel: EtatMateriel?,
    val typeAffectation: String?,
    val departementNom: String?,
    val active: Boolean
)
