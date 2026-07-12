package com.mindforce.mindlog.data.repository

import com.google.gson.Gson
import com.mindforce.mindlog.data.model.PanneRequest
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PanneRepository(private val api: ApiService) {

    /**
     * Signale une panne avec sa photo justificative obligatoire.
     * @param photoFile fichier image (pris par la caméra ou choisi dans la galerie, déjà copié en local)
     */
    suspend fun signaler(request: PanneRequest, photoFile: File): ApiResult<PanneResponse> {
        return try {
            val json = Gson().toJson(request)
            val dataBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            val mimeType = when (photoFile.extension.lowercase()) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                "heic" -> "image/heic"
                else -> "image/jpeg"
            }
            val photoBody = photoFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoBody)

            val response = api.signalerPanne(dataBody, photoPart)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun getMesSignalements(): ApiResult<List<PanneResponse>> {
        return try {
            val response = api.getMesSignalements()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun getHistorique(materielId: String): ApiResult<List<PanneResponse>> {
        return try {
            val response = api.getHistoriquePannes(materielId)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }
}
