package com.example.healthtracker

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder

class FitnessDataService : Service() {
    private val handler = Handler()
    private val interval = 2000L // Интервал предачи данных в миллисекундах

    private val runnable = object : Runnable {
        override fun run() {
            val data = MockFitnessTracker.generateData()
            sendDataToActivity(data)
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun sendDataToActivity(data: FitnessData) {
        val intent = Intent("FitnessDataUpdate")
        intent.putExtra("heartRate", data.heartRate)
        intent.putExtra("saturation", data.saturation)
        intent.putExtra("isActive", data.isActive)
        sendBroadcast(intent)
    }
}