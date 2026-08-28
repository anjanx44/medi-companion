package com.medicompanion.app

import android.app.Application
import com.google.firebase.FirebaseApp

class MediCompanionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
