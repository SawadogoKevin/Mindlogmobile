package com.mindforce.mindlog.data.remote

import com.mindforce.mindlog.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== AUTH =====

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/verify")
    suspend fun verify(@Body request: VerificationRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<String>

    // ===== DASHBOARD =====

    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats>

    // ===== MATERIELS (Chef de Département) =====

    /** Matériels affectés au département du chef connecté */
    @GET("api/affectations/departement/{departementId}/actives")
    suspend fun getMesMateriels(@Path("departementId") departementId: Long): Response<List<MaterielResponse>>

    @GET("api/materiels/{id}")
    suspend fun getMateriel(@Path("id") id: String): Response<MaterielResponse>

    // ===== PANNES (Chef de Département) =====

    /**
     * Signale une panne — multipart/form-data
     * "data"  : JSON (PanneRequest)
     * "photo" : photo justificative (obligatoire, prise par l'appareil photo ou choisie en galerie)
     */
    @Multipart
    @POST("api/pannes")
    suspend fun signalerPanne(
        @Part("data") data: RequestBody,
        @Part photo: MultipartBody.Part
    ): Response<PanneResponse>

    @GET("api/pannes")
    suspend fun getMesSignalements(): Response<List<PanneResponse>>

    @GET("api/pannes/materiel/{materielId}")
    suspend fun getHistoriquePannes(@Path("materielId") materielId: String): Response<List<PanneResponse>>
}
