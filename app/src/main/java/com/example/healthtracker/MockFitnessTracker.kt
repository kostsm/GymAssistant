package com.example.healthtracker

import kotlin.random.Random
import kotlin.math.roundToInt

object MockFitnessTracker {
    private val random = Random

    val scale = 0.5

    // Дефолтные значения (для старта генерации)
    private var lastHeartRate: Int = random.nextInt(60, 100)
    private var lastSaturation: Int = random.nextInt(90, 100)
    private var lastActivity: Boolean = false

    // Дефолтные параметры пользователя
    private var userAge: Int = 30
    private var userFitnessLevel: Int = 1
    private var userSmoking: Boolean = false
    private var userAlcohol: Boolean = false

    // Устанавливаем параметры пользователя
    fun setUserParams(age: Int, fitnessLevel: Int, smoking: Boolean, alcohol: Boolean) {
        userAge = age
        userFitnessLevel = fitnessLevel
        userSmoking = smoking
        userAlcohol = alcohol
    }

    // Генерация пульса с учетом активности и параметров пользователя
    private fun generateHeartRate(isActive: Boolean): Int {
        val maxHeartRate = 220 - userAge
        val fitnessFactor = when (userFitnessLevel) {
            0 -> 1.2
            1 -> 1.1
            2 -> 1.0
            3 -> 0.9
            else -> 1.0
        }
        val baseHeartRate = if (isActive) {
            (maxHeartRate * fitnessFactor * random.nextDouble(0.7, 0.9)).roundToInt()
        } else {
            (maxHeartRate * fitnessFactor * random.nextDouble(0.5, 0.7)).roundToInt()
        }
        lastHeartRate = (lastHeartRate * scale + baseHeartRate * (1 - scale)).toInt()
        return lastHeartRate.coerceIn(50, maxHeartRate)
    }

    // Генерация сатурации с учетом активности и вредных привычек
    private fun generateSaturation(isActive: Boolean): Int {
        val baseSaturation = if (isActive) {
            random.nextInt(90, 95)
        } else {
            random.nextInt(95, 100)
        }
        val smokingPenalty = if (userSmoking) -5 else 0
        val alcoholPenalty = if (userAlcohol) -3 else 0

        lastSaturation = (lastSaturation * scale + (baseSaturation + smokingPenalty + alcoholPenalty) * (1 - scale)).toInt()
        return lastSaturation.coerceIn(85, 100)
    }

    // Генерация активности, шанс сменить 0.3*рандом (то есть меньше 30% всегда)
    private fun generateActivityStatus(): Boolean {
        if (random.nextInt(0, 100) > 70)
            lastActivity = random.nextBoolean()
        return lastActivity
    }

    fun generateData(): FitnessData {
        val isActive = generateActivityStatus()
        return FitnessData(
            heartRate = generateHeartRate(isActive),
            saturation = generateSaturation(isActive),
            isActive = isActive
        )
    }
}
