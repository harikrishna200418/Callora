package com.callora.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CalloraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization logic for battery monitor, WebRTC, etc.
    }
}
