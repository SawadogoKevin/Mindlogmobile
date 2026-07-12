package com.mindforce.mindlog

import android.app.Application
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.RetrofitClient
import com.mindforce.mindlog.data.repository.AuthRepository
import com.mindforce.mindlog.data.repository.DashboardRepository
import com.mindforce.mindlog.data.repository.MaterielRepository
import com.mindforce.mindlog.data.repository.PanneRepository

class MindLogApp : Application() {

    lateinit var sessionManager: SessionManager
        private set
    lateinit var apiService: ApiService
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var materielRepository: MaterielRepository
        private set
    lateinit var panneRepository: PanneRepository
        private set
    lateinit var dashboardRepository: DashboardRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        apiService = RetrofitClient.create(sessionManager)
        authRepository = AuthRepository(apiService, sessionManager)
        materielRepository = MaterielRepository(apiService)
        panneRepository = PanneRepository(apiService)
        dashboardRepository = DashboardRepository(apiService)
    }
}
