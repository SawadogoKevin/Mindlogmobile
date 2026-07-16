package com.mindforce.mindlog.data.model

data class UserResponse(
    val id: Long,
    val nom: String,
    val prenom: String,
    val email: String,
    val telephone: String?,
    val role: String,
    val matricule: String?,
    val permissions: List<String>? = null
)

data class UpdateProfileRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val telephone: String
)

data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)
