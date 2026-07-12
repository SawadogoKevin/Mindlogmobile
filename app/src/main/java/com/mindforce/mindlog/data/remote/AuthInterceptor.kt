package com.mindforce.mindlog.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import com.mindforce.mindlog.data.local.SessionManager

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Les endpoints /api/auth/** n'ont pas besoin de token
        if (original.url.encodedPath.startsWith("/api/auth")) {
            return chain.proceed(original)
        }

        val token = runBlocking { sessionManager.getToken() }
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
