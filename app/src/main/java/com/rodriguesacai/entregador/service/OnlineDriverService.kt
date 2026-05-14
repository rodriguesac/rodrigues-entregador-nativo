package com.rodriguesacai.entregador.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.rodriguesacai.entregador.data.DriverRepository

class OnlineDriverService : Service() {
    override fun onCreate() {
        super.onCreate()
        DriverRepository.setOnline(this, true)
        startForeground(10, NotificationHelper.onlineNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DriverRepository.setOnline(this, true)
        return START_STICKY
    }

    override fun onDestroy() {
        DriverRepository.setOnline(this, false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
