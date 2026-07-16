package com.mindforce.mindlog.data.repository

import com.mindforce.mindlog.data.model.UpdatePasswordRequest
import com.mindforce.mindlog.data.model.UpdateProfileRequest
import com.mindforce.mindlog.data.model.UserResponse
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils

class UserRepository(private val api: ApiService) {

    suspend fun getMyProfile(): ApiResult<UserResponse> {
        return try {
            val response = api.getMyProfile()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun updateMyProfile(request: UpdateProfileRequest): ApiResult<UserResponse> {
        return try {
            val response = api.updateMyProfile(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun updateMyPassword(request: UpdatePasswordRequest): ApiResult<String> {
        return try {
            val response = api.updateMyPassword(request)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: "Mot de passe modifié avec succès")
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }
}
