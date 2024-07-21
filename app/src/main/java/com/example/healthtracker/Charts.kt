package com.example.healthtracker

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter

fun addEntryToChart(chart: LineChart, entry: Entry, label: String, criticalValue: Float, fillAbove: Boolean) {
    val data = chart.data

    var dataSet: LineDataSet? = data.getDataSetByLabel(label, true) as LineDataSet?
    if (dataSet == null) {
        dataSet = createDataSet(label)
        data.addDataSet(dataSet)
    }

    dataSet.addEntry(entry)

    // Добавление точки в критическую линию
    var criticalDataSet: LineDataSet? = data.getDataSetByLabel("Критическая $label", true) as LineDataSet?
    if (criticalDataSet == null) {
        criticalDataSet = createCriticalDataSet(chart, "Критическая $label", criticalValue, fillAbove)
        data.addDataSet(criticalDataSet)
    }

    criticalDataSet.addEntry(Entry(entry.x, criticalValue))

    data.notifyDataChanged()
    chart.notifyDataSetChanged()
    chart.setVisibleXRangeMaximum(20f)
    chart.moveViewToX(data.entryCount.toFloat() - 20)
}

fun createCriticalDataSet(chart: LineChart, label: String, value: Float, fillAbove: Boolean): LineDataSet {
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

fun createDataSet(label: String): LineDataSet {
    val dataSet = LineDataSet(null, label)
    dataSet.lineWidth = 2.5f
    dataSet.setDrawCircles(false)
    dataSet.setDrawValues(false)
    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
    dataSet.color = Color.parseColor("#4A90E2")
    return dataSet
}

fun initializeCharts(chart: LineChart, label: String, value: Float, fillAbove: Boolean) {
    chart.data = LineData()
    addCriticalLineToChart(chart, label, value, fillAbove)
    chart.invalidate()
}

private fun addCriticalLineToChart(chart: LineChart, label: String, value: Float, fillAbove: Boolean) {
    val entries = mutableListOf<Entry>()
    for (i in 0 until 20) {
        entries.add(Entry(i.toFloat(), value))
    }

    val dataSet = createCriticalDataSet(chart, label, value, fillAbove)
    chart.data.addDataSet(dataSet)
}

fun updateCharts(data: FitnessData, heartRateChart: LineChart?, saturationChart: LineChart?) {
    heartRateChart?.let { chart ->
        val heartRateEntry = Entry((chart.data.entryCount + 1).toFloat(), data.heartRate.toFloat())
        addEntryToChart(chart, heartRateEntry, "ЧСС", 180f, true)
    }

    saturationChart?.let { chart ->
        val saturationEntry = Entry((chart.data.entryCount + 1).toFloat(), data.saturation.toFloat())
        addEntryToChart(chart, saturationEntry, "Сатурация", 90f, false)
    }
}

fun updateUI(data: FitnessData, setHeartRate: (Int) -> Unit, setSaturation: (Int) -> Unit, setIsActive: (Boolean) -> Unit) {
    setHeartRate(data.heartRate)
    setSaturation(data.saturation)
    setIsActive(data.isActive)
}