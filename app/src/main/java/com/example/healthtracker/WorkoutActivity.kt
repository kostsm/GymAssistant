package com.example.healthtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.database.User
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class WorkoutActivity : AppCompatActivity() {

    private lateinit var heartRateTextView: TextView
    private lateinit var saturationTextView: TextView
    private lateinit var activityStatusTextView: TextView
    private lateinit var healthStatusTextView: TextView
    private lateinit var activityIndicator: View

    private lateinit var heartRateChart: LineChart
    private lateinit var saturationChart: LineChart

    private lateinit var user: User
    private lateinit var mediaPlayer: MediaPlayer

    private val fitnessDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val heartRate = it.getIntExtra("heartRate", 0)
                val saturation = it.getIntExtra("saturation", 0)
                val isActive = it.getBooleanExtra("isActive", false)
                val data = FitnessData(heartRate, saturation, isActive)

                updateUI(data)
                evaluateHealth(data)
                updateCharts(data)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        heartRateTextView = findViewById(R.id.heartRateTextView)
        saturationTextView = findViewById(R.id.saturationTextView)
        activityStatusTextView = findViewById(R.id.activityStatusTextView)
        healthStatusTextView = findViewById(R.id.healthStatusTextView)
        activityIndicator = findViewById(R.id.activityIndicator)

        heartRateChart = findViewById(R.id.heartRateChart)
        saturationChart = findViewById(R.id.saturationChart)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "health-tracker-db"
        ).allowMainThreadQueries().build()

        user = db.userDao().getUser()!!

        // Устанавливаем пользовательские параметры для генератора данных
        MockFitnessTracker.setUserParams(user.age, user.trainingLevel, user.smoking, user.drinking)

        mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound)

        registerReceiver(fitnessDataReceiver, IntentFilter("FitnessDataUpdate"))
        startService(Intent(this, FitnessDataService::class.java))

        initializeCharts()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(fitnessDataReceiver)
        stopService(Intent(this, FitnessDataService::class.java))
    }

    private fun initializeCharts() {
        heartRateChart.data = LineData()
        saturationChart.data = LineData()

        addCriticalLineToChart(heartRateChart, "Критическая ЧСС", 180f, true)
        addCriticalLineToChart(saturationChart, "Критическая Сатурация", 90f, false)

        heartRateChart.invalidate()
        saturationChart.invalidate()
    }

    private fun addCriticalLineToChart(chart: LineChart, label: String, value: Float, fillAbove: Boolean) {
        val entries = mutableListOf<Entry>()
        for (i in 0 until 20) {
            entries.add(Entry(i.toFloat(), value))
        }

        val dataSet = createCriticalDataSet(chart, label, value, fillAbove)
        chart.data.addDataSet(dataSet)
    }

    private fun updateUI(data: FitnessData) {
        heartRateTextView.text = "ЧСС: ${data.heartRate}"
        saturationTextView.text = "Сатурация: ${data.saturation}"
        activityStatusTextView.text = if (data.isActive) "Активен" else "Неактивен"
        activityIndicator.setBackgroundColor(
            if (data.isActive) Color.GREEN else Color.RED
        )
    }

    private fun evaluateHealth(data: FitnessData) {
        val criticalHeartRate = calculateCriticalHeartRate(user, data.isActive)
        val criticalSaturation = calculateCriticalSaturation(user, data.isActive)

        val heartRateStatus = if (data.heartRate in criticalHeartRate) "Норма" else "Опасность"
        val saturationStatus = if (data.saturation in criticalSaturation) "Норма" else "Опасность"

        healthStatusTextView.text = "Пульс: $heartRateStatus, Сатурация: $saturationStatus"

        // Воспроизведение звукового сигнала при опасном состоянии
        if (heartRateStatus == "Опасность" || saturationStatus == "Опасность") {
            playWarningSound()
        }
    }

    private fun updateCharts(data: FitnessData) {
        val heartRateEntry = Entry((heartRateChart.data.entryCount + 1).toFloat(), data.heartRate.toFloat())
        val saturationEntry = Entry((saturationChart.data.entryCount + 1).toFloat(), data.saturation.toFloat())

        addEntryToChart(heartRateChart, heartRateEntry, "ЧСС", 180f, true) // fillAbove = true для пульса
        addEntryToChart(saturationChart, saturationEntry, "Сатурация", 90f, false) // fillAbove = false для сатурации
    }

    private fun addEntryToChart(chart: LineChart, entry: Entry, label: String, criticalValue: Float, fillAbove: Boolean) {
        val data = chart.data

        var dataSet: ILineDataSet? = data.getDataSetByLabel(label, true)
        if (dataSet == null) {
            dataSet = createDataSet(label)
            data.addDataSet(dataSet)
        }

        (dataSet as LineDataSet).addEntry(entry)

        // Добавление точки в критическую линию
        var criticalDataSet: ILineDataSet? = data.getDataSetByLabel("Критическая $label", true)
        if (criticalDataSet == null) {
            criticalDataSet = createCriticalDataSet(chart, "Критическая $label", criticalValue, fillAbove)
            data.addDataSet(criticalDataSet)
        }

        (criticalDataSet as LineDataSet).addEntry(Entry(entry.x, criticalValue))

        data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.setVisibleXRangeMaximum(20f)
        chart.moveViewToX(data.entryCount.toFloat() - 20)
    }

    private fun createCriticalDataSet(chart: LineChart, label: String, value: Float, fillAbove: Boolean): LineDataSet {
        val entries = mutableListOf<Entry>()
        for (i in 0 until 20) {
            entries.add(Entry(i.toFloat(), value))
        }

        val dataSet = LineDataSet(entries, label)
        dataSet.lineWidth = 2.5f
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.enableDashedLine(10f, 5f, 0f)
        dataSet.color = Color.RED
        dataSet.setDrawFilled(true)
        dataSet.fillFormatter = IFillFormatter { _, _ ->
            if (fillAbove) chart.yChartMax else chart.yChartMin
        }
        dataSet.fillColor = Color.RED
        dataSet.fillAlpha = 50
        return dataSet
    }

    private fun createDataSet(label: String): LineDataSet {
        val dataSet = LineDataSet(null, label)
        dataSet.lineWidth = 2.5f
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.color = ContextCompat.getColor(this, R.color.primaryColor)
        return dataSet
    }

    private fun calculateCriticalHeartRate(user: User, isActive: Boolean): IntRange {
        val maxHeartRate = 220 - user.age
        val lowerBound = if (isActive) 90 else 60  // Устанавливаем нижнюю границу пульса
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
        return if (bmi < 18.5) {
            0.9
        } else if (bmi < 24.9) {
            1.0
        } else {
            0.8
        }
    }

    private fun getTrainingLevelFactor(user: User): Double {
        return when (user.trainingLevel) {
            0 -> 1.2  // Новичок
            1 -> 1.1  // Любитель
            2 -> 1.0  // Продвинутый
            3 -> 0.9  // Профессионал
            else -> 1.0
        }
    }

    private fun playWarningSound() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }
}
