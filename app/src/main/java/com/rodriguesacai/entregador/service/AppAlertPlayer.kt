package com.rodriguesacai.entregador.service

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.rodriguesacai.entregador.R

object AppAlertPlayer {
    private var lastPlayedAt = 0L

    fun playNewRide(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastPlayedAt < 2500L) return
        lastPlayedAt = now

        runCatching {
            val appContext = context.applicationContext
            MediaPlayer.create(appContext, R.raw.alerta)?.apply {
                setOnCompletionListener { player ->
                    runCatching { player.release() }
                }
                start()
            }
        }
        vibrate(context)
    }

    private fun vibrate(context: Context) {
        runCatching {
            val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(VibratorManager::class.java)
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 450, 160, 450, 160, 750)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}
