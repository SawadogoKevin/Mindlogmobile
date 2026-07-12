package com.mindforce.mindlog.data.model

data class DashboardStats(
    val totalMateriels: Int,
    val materielsEnPanne: Int,
    val signalementsEnAttente: Int,
    val signalementsResolus: Int
)
