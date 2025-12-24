package com.gymshark.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.gymshark.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
/*
* графік прогрес ваги
* середній пульс
* кількість ккал на день
* час тренувань
* почуття після тренування
*
* */
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var isFullMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chart = view.findViewById<LineChart>(R.id.lineChart)

        // Уявімо, що це всі дані (потім заміниш на дані з БД)
        val allEntries = (1..30).map { x ->
            Entry(x.toFloat(), (10..25).random().toFloat())
        }

        fun render(lastOnly: Boolean) {
            val entriesToShow = if (lastOnly) allEntries.takeLast(5) else allEntries

            val dataSet = LineDataSet(entriesToShow, if (lastOnly) "Last 5" else "All").apply {
                setDrawValues(false)
                setDrawCircles(true)
                circleRadius = 4f
                lineWidth = 2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)

            chart.description.isEnabled = false
            chart.axisRight.isEnabled = false

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            chart.axisLeft.axisMinimum = 0f

            if (lastOnly) {
                // Локальний режим: показати тільки останні 5 (без скролу)
                chart.setTouchEnabled(true)
                chart.setScaleEnabled(false)
                chart.isDragEnabled = false
            } else {
                // Повний режим: скрол/зум по X
                chart.setTouchEnabled(true)
                chart.isDragEnabled = true
                chart.setScaleEnabled(true)
                chart.setPinchZoom(true)

                // Щоб було реально "довго" і горталось:
                chart.setVisibleXRangeMaximum(7f) // скільки точок видно одночасно
                chart.moveViewToX(allEntries.last().x) // старт з кінця (останній запис)
            }

            chart.invalidate()
        }

        // старт: тільки 5 останніх
        render(lastOnly = true)

        // по тапу — перемикаємось у full mode
        chart.setOnClickListener {
            isFullMode = !isFullMode
            render(lastOnly = !isFullMode)
        }
    }
}

