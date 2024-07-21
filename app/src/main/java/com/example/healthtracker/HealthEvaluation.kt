package com.example.healthtracker

import android.media.MediaPlayer
import com.example.healthtracker.database.User

fun evaluateHealth(data: FitnessData, user: User, mediaPlayer: MediaPlayer) {
    val criticalHeartRate = calculateCriticalHeartRate(user, data.isActive)
    val criticalSaturation = calculateCriticalSaturation(user, data.isActive)

    val heartRateStatus = if (data.heartRate in criticalHeartRate) "Норма" else "Опасность"
    val saturationStatus = if (data.saturation in criticalSaturation) "Норма" else "Опасность"

    // Воспроизведение звукового сигнала при опасном состоянии
    if (heartRateStatus == "Опасность" || saturationStatus == "Опасность") {
        playWarningSound(mediaPlayer)
    }
}

private fun calculateCriticalHeartRate(user: User, isActive: Boolean): IntRange {
    val maxHeartRate = 220 - user.age
    val lowerBound = if (isActive) 90 else 60
    val upperBound = if (isActive) {
        (0.9 * maxHeartRate * getTrainingLevelFactor(user) * getBmiFactor(user)).toInt()
    } else {
        (0.75 * maxHeartRate * getTrainingLevelFactor(user) * getBmiFactor(user)).toInt()
    }
    return lowerBound..upperBound
}

private fun calculateCriticalSaturation(user: User, isActive: Boolean): IntRange {
    val lowerBound = if (user.smoking) 85 else 90
    val upperBound = 100
    return lowerBound..upperBound
}

private fun getBmiFactor(user: User): Double {
    val heightInMeters = user.height / 100.0
    val bmi = user.weight / (heightInMeters * heightInMeters)
    return when {
        bmi < 18.5 -> 0.9
        bmi < 24.9 -> 1.0
        else -> 0.8
    }
}

private fun getTrainingLevelFactor(user: User): Double {
    return when (user.trainingLevel) {
        0 -> 1.2
        1 -> 1.1
        2 -> 1.0
        3 -> 0.9
        else -> 1.0
    }
}

private fun playWarningSound(mediaPlayer: MediaPlayer) {
    if (!mediaPlayer.isPlaying) {
        mediaPlayer.start()
    }
}
