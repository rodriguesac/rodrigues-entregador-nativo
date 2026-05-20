package com.rodriguesacai.entregador

import android.app.Application
import com.google.firebase.FirebaseApp

class RodriguesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
