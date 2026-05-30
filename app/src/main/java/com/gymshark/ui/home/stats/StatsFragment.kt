package com.gymshark.ui.home.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.gymshark.R
import com.gymshark.databinding.FragmentStatsBinding
import com.gymshark.domain.models.MoodUi
import com.gymshark.ui.home.stats.viewmodel.StatsViewModel
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val vm: StatsViewModel by activityViewModel()

    private var isWeightFull = false
    private var isDurationFull = false
    private var isCaloriesFull = false
    private var isBpmFull = false
    private var isCategoryWeightFull = false
    private var isCategorySetsFull = false
    private var isVolumeFull = false
    private var isOneRmFull = false

    private val chartLineColors: List<Int>
        get() = listOf(
            color(R.color.chartBlue),
            color(R.color.chartGreen),
            color(R.color.chartAmber),
            color(R.color.chartRose),
            color(R.color.chartViolet),
            color(R.color.chartTeal),
            color(R.color.chartLime),
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEmptyStates()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.weightProgress.collect { points ->
                renderLineChart(binding.lineChart, points, isWeightFull, color(R.color.chartBlue))
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.durationProgress.collect { points ->
                renderLineChart(binding.lineChartDuration, points, isDurationFull, color(R.color.trainingAccent))
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.caloriesProgress.collect { points ->
                renderLineChart(binding.lineChartCalories, points, isCaloriesFull, color(R.color.chartAmber))
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.bpmProgress.collect { points ->
                renderLineChart(binding.lineChartBpm, points, isBpmFull, color(R.color.chartRose))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.dailyStats.collect { list ->
                val today = list.firstOrNull() ?: return@collect
                binding.tvTime.text = "Training time: ${today.totalMinutes} min"
                binding.tvCalories.text = "Calories: ${today.calories}"
                binding.tvBpm.text = today.avgBpm?.let { "Avg BPM: $it" } ?: "Avg BPM: not measured"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.moodTimeline.collect { list ->
                binding.moodContainer.removeAllViews()
                list.forEach { item ->
                    val tv = TextView(requireContext()).apply {
                        text = when (item.mood) {
                            MoodUi.BAD -> "Bad"
                            MoodUi.NEUTRAL -> "Neutral"
                            MoodUi.GOOD -> "Good"
                            MoodUi.AMAZING -> "Amazing"
                        }
                        textSize = 15f
                        setTextColor(color(R.color.textPrimary))
                        setBackgroundResource(R.drawable.bg_stats_chip)
                        setPadding(18, 8, 18, 8)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginEnd = 8
                        }
                    }
                    binding.moodContainer.addView(tv)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.weeklyMood.collect { mood ->
                binding.tvWeeklyMood.text = mood?.let { "This week: $it" } ?: "This week: -"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.categoryDailyStats.collect { rows ->
                val groups = rows.groupBy { it.category }
                    .mapValues { (_, v) -> v.map { it.day to it.maxWeight } }
                val dates = rows.map { it.day }.distinct().sorted()
                renderMultiLineChart(binding.lineChartCategoryWeight, groups, dates, isCategoryWeightFull)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.categoryDailyStats.collect { rows ->
                val groups = rows.groupBy { it.category }
                    .mapValues { (_, v) -> v.map { it.day to it.totalSets.toFloat() } }
                val dates = rows.map { it.day }.distinct().sorted()
                renderMultiLineChart(binding.lineChartCategorySets, groups, dates, isCategorySetsFull)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.weeklyVolume.collect { rows ->
                val points = rows.mapIndexed { i, r ->
                    ChartPoint(i.toFloat(), r.totalVolume, r.week.substringAfter("-"))
                }
                renderLineChart(binding.lineChartVolume, points, isVolumeFull, color(R.color.chartGreen))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.topExerciseOneRM.collect { rows ->
                val groups = rows.groupBy { it.exerciseName }
                    .mapValues { (_, v) -> v.map { it.day to it.estimated1RM } }
                val dates = rows.map { it.day }.distinct().sorted()
                renderMultiLineChart(binding.lineChartOneRM, groups, dates, isOneRmFull)
            }
        }

        binding.lineChart.setOnClickListener {
            isWeightFull = !isWeightFull
            renderLineChart(binding.lineChart, vm.weightProgress.value, isWeightFull, color(R.color.chartBlue))
        }
        binding.lineChartDuration.setOnClickListener {
            isDurationFull = !isDurationFull
            renderLineChart(binding.lineChartDuration, vm.durationProgress.value, isDurationFull, color(R.color.trainingAccent))
        }
        binding.lineChartCalories.setOnClickListener {
            isCaloriesFull = !isCaloriesFull
            renderLineChart(binding.lineChartCalories, vm.caloriesProgress.value, isCaloriesFull, color(R.color.chartAmber))
        }
        binding.lineChartBpm.setOnClickListener {
            isBpmFull = !isBpmFull
            renderLineChart(binding.lineChartBpm, vm.bpmProgress.value, isBpmFull, color(R.color.chartRose))
        }
        binding.lineChartVolume.setOnClickListener {
            isVolumeFull = !isVolumeFull
            val points = vm.weeklyVolume.value.mapIndexed { i, r ->
                ChartPoint(i.toFloat(), r.totalVolume, r.week.substringAfter("-"))
            }
            renderLineChart(binding.lineChartVolume, points, isVolumeFull, color(R.color.chartGreen))
        }

        binding.lineChartCategoryWeight.setOnClickListener {
            isCategoryWeightFull = !isCategoryWeightFull
            val rows = vm.categoryDailyStats.value
            val groups = rows.groupBy { it.category }
                .mapValues { (_, v) -> v.map { it.day to it.maxWeight } }
            val dates = rows.map { it.day }.distinct().sorted()
            renderMultiLineChart(binding.lineChartCategoryWeight, groups, dates, isCategoryWeightFull)
        }
        binding.lineChartCategorySets.setOnClickListener {
            isCategorySetsFull = !isCategorySetsFull
            val rows = vm.categoryDailyStats.value
            val groups = rows.groupBy { it.category }
                .mapValues { (_, v) -> v.map { it.day to it.totalSets.toFloat() } }
            val dates = rows.map { it.day }.distinct().sorted()
            renderMultiLineChart(binding.lineChartCategorySets, groups, dates, isCategorySetsFull)
        }
        binding.lineChartOneRM.setOnClickListener {
            isOneRmFull = !isOneRmFull
            val rows = vm.topExerciseOneRM.value
            val groups = rows.groupBy { it.exerciseName }
                .mapValues { (_, v) -> v.map { it.day to it.estimated1RM } }
            val dates = rows.map { it.day }.distinct().sorted()
            renderMultiLineChart(binding.lineChartOneRM, groups, dates, isOneRmFull)
        }
    }

    private fun renderLineChart(
        chart: LineChart,
        points: List<ChartPoint>,
        showAll: Boolean,
        lineColor: Int
    ) {
        if (points.isEmpty()) {
            chart.clear()
            return
        }

        val visible = if (showAll) points else points.takeLast(DEFAULT_VISIBLE_POINTS)
        val entries = visible.mapIndexed { index, point -> Entry(index.toFloat(), point.y) }
        val labels = visible.map { it.label }

        val mainDataSet = LineDataSet(entries, "").apply {
            color = lineColor
            setCircleColor(lineColor)
            setDrawValues(false)
            setDrawCircleHole(false)
            setDrawCircles(true)
            circleRadius = 3.5f
            lineWidth = 2.4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val maxDataSet = entries.maxByOrNull { it.y }?.let {
            LineDataSet(listOf(it), "").apply {
                lineWidth = 0f
                setDrawCircles(true)
                setDrawCircleHole(false)
                circleRadius = 6f
                setCircleColor(color(R.color.chartPeak))
                setDrawValues(true)
                valueTextSize = 10f
                valueTextColor = color(R.color.chartPeak)
            }
        }

        chart.axisLeft.removeAllLimitLines()
        if (entries.size > 1) {
            val avg = entries.map { it.y }.average().toFloat()
            chart.axisLeft.addLimitLine(
                LimitLine(avg, "avg ${avg.toInt()}").apply {
                    this.lineColor = color(R.color.chartGrid)
                    lineWidth = 1f
                    enableDashedLine(10f, 6f, 0f)
                    textColor = color(R.color.textSecondary)
                    textSize = 9f
                }
            )
        }

        chart.data = LineData(listOfNotNull(mainDataSet, maxDataSet))
        styleChart(chart, labels, entries)
    }

    private fun renderMultiLineChart(
        chart: LineChart,
        groups: Map<String, List<Pair<String, Float>>>,
        allDates: List<String>,
        showAll: Boolean
    ) {
        if (groups.isEmpty() || allDates.isEmpty()) {
            chart.clear()
            return
        }

        val visibleDates = if (showAll) allDates else allDates.takeLast(DEFAULT_VISIBLE_POINTS)
        val dateIndex = visibleDates.withIndex().associate { (i, d) -> d to i.toFloat() }
        val shortLabels = visibleDates.map { it.toShortDateLabel() }
        val colors = chartLineColors

        val dataSets = groups.entries.mapIndexedNotNull { index, (groupName, points) ->
            val entries = points
                .filter { (day, _) -> day in dateIndex }
                .map { (day, value) -> Entry(dateIndex.getValue(day), value) }
                .sortedBy { it.x }
            if (entries.isEmpty()) return@mapIndexedNotNull null

            val lineColor = colors[index % colors.size]
            LineDataSet(entries, groupName).apply {
                color = lineColor
                setCircleColor(lineColor)
                lineWidth = 2.2f
                circleRadius = 3f
                mode = LineDataSet.Mode.LINEAR
                setDrawValues(false)
                setDrawCircleHole(false)
            }
        }

        if (dataSets.isEmpty()) {
            chart.clear()
            return
        }

        chart.data = LineData(dataSets)
        chart.legend.apply {
            isEnabled = true
            textColor = color(R.color.textSecondary)
            textSize = 10f
            formSize = 8f
            xEntrySpace = 12f
        }
        styleChart(chart, shortLabels, dataSets.flatMap { it.values.map { value -> Entry(value.x, value.y) } })
    }

    private fun styleChart(chart: LineChart, labels: List<String>, entries: List<Entry>) {
        val axisTextColor = color(R.color.textSecondary)
        val gridColor = color(R.color.chartGrid)

        chart.apply {
            description.isEnabled = false
            setDrawBorders(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setBackgroundColor(Color.TRANSPARENT)
            setNoDataTextColor(axisTextColor)
            setExtraOffsets(6f, 8f, 10f, 8f)
        }

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = axisTextColor
            valueFormatter = IndexAxisValueFormatter(labels)
            labelRotationAngle = -30f
            labelCount = labels.size.coerceAtMost(6)
            axisMinimum = -0.2f
            axisMaximum = (labels.lastIndex + 0.2f).coerceAtLeast(0.8f)
        }

        val minY = entries.minOf { it.y }
        val maxY = entries.maxOf { it.y }
        val padding = ((maxY - minY) * 0.18f).coerceAtLeast(1f)
        chart.axisLeft.apply {
            axisMinimum = if (minY >= 0f) (minY - padding).coerceAtLeast(0f) else minY - padding
            axisMaximum = maxY + padding
            textColor = axisTextColor
            setGridColor(gridColor)
            setAxisLineColor(gridColor)
            setDrawZeroLine(false)
        }
        chart.axisRight.isEnabled = false

        if (chart.legend.isEnabled) {
            chart.legend.textColor = axisTextColor
        }

        val maxVisible = DEFAULT_VISIBLE_POINTS.toFloat()
        if (labels.size > DEFAULT_VISIBLE_POINTS) {
            chart.setVisibleXRangeMaximum(maxVisible)
            chart.moveViewToX(labels.lastIndex.toFloat())
        } else {
            chart.fitScreen()
        }

        chart.invalidate()
    }

    private fun setupEmptyStates() {
        listOf(
            binding.lineChart,
            binding.lineChartDuration,
            binding.lineChartCalories,
            binding.lineChartBpm,
            binding.lineChartCategoryWeight,
            binding.lineChartCategorySets,
            binding.lineChartVolume,
            binding.lineChartOneRM
        ).forEach {
            it.setNoDataText("No data yet")
            it.setNoDataTextColor(color(R.color.textSecondary))
        }
    }

    private fun String.toShortDateLabel(): String =
        if (length >= 10) substring(5).replace("-", ".") else this

    private fun color(resId: Int): Int =
        ContextCompat.getColor(requireContext(), resId)

    private companion object {
        const val DEFAULT_VISIBLE_POINTS = 12
    }
}
