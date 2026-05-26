package com.gymshark.ui.home.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

    private val lineColors = listOf(
        Color.parseColor("#4FC3F7"),
        Color.parseColor("#81C784"),
        Color.parseColor("#FFB74D"),
        Color.parseColor("#F06292"),
        Color.parseColor("#CE93D8"),
        Color.parseColor("#4DB6AC"),
        Color.parseColor("#FFF176"),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            vm.weightProgress.collect { points ->
                if (points.isNotEmpty()) renderLineChart(binding.lineChart, points, !isWeightFull)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.durationProgress.collect { points ->
                if (points.isNotEmpty()) renderLineChart(binding.lineChartDuration, points, !isDurationFull)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.caloriesProgress.collect { points ->
                if (points.isNotEmpty()) renderLineChart(binding.lineChartCalories, points, !isCaloriesFull)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.bpmProgress.collect { points ->
                if (points.isNotEmpty()) renderLineChart(binding.lineChartBpm, points, !isBpmFull)
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
                        textSize = 24f
                        setPadding(12, 0, 12, 0)
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
                if (rows.isEmpty()) return@collect
                val groups = rows.groupBy { it.category }
                    .mapValues { (_, v) -> v.map { it.day to it.maxWeight } }
                val dates = rows.map { it.day }.distinct().sorted()
                renderMultiLineChart(binding.lineChartCategoryWeight, groups, dates, !isCategoryWeightFull)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.categoryDailyStats.collect { rows ->
                if (rows.isEmpty()) return@collect
                val groups = rows.groupBy { it.category }
                    .mapValues { (_, v) -> v.map { it.day to it.totalSets.toFloat() } }
                val dates = rows.map { it.day }.distinct().sorted()
                renderMultiLineChart(binding.lineChartCategorySets, groups, dates, !isCategorySetsFull)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.weeklyVolume.collect { rows ->
                if (rows.isEmpty()) return@collect
                val points = rows.mapIndexed { i, r -> ChartPoint(i.toFloat(), r.totalVolume) }
                val labels = rows.map { it.week.takeLast(3) }
                renderLineChart(binding.lineChartVolume, points, !isVolumeFull, xLabels = labels)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.topExerciseOneRM.collect { rows ->
                if (rows.isEmpty()) return@collect
                val groups = rows.groupBy { it.exerciseName }
                    .mapValues { (_, v) -> v.map { it.day to it.estimated1RM } }
                val dates = rows.map { it.day }.distinct().sorted()
                renderMultiLineChart(binding.lineChartOneRM, groups, dates, !isOneRmFull)
            }
        }

        binding.lineChart.setOnClickListener {
            isWeightFull = !isWeightFull
            renderLineChart(binding.lineChart, vm.weightProgress.value, !isWeightFull)
        }
        binding.lineChartDuration.setOnClickListener {
            isDurationFull = !isDurationFull
            renderLineChart(binding.lineChartDuration, vm.durationProgress.value, !isDurationFull)
        }
        binding.lineChartCalories.setOnClickListener {
            isCaloriesFull = !isCaloriesFull
            renderLineChart(binding.lineChartCalories, vm.caloriesProgress.value, !isCaloriesFull)
        }
        binding.lineChartBpm.setOnClickListener {
            isBpmFull = !isBpmFull
            renderLineChart(binding.lineChartBpm, vm.bpmProgress.value, !isBpmFull)
        }
        binding.lineChartVolume.setOnClickListener {
            isVolumeFull = !isVolumeFull
            val rows = vm.weeklyVolume.value
            val points = rows.mapIndexed { i, r -> ChartPoint(i.toFloat(), r.totalVolume) }
            val labels = rows.map { it.week.takeLast(3) }
            renderLineChart(binding.lineChartVolume, points, !isVolumeFull, xLabels = labels)
        }

        binding.lineChartCategoryWeight.setOnClickListener {
            isCategoryWeightFull = !isCategoryWeightFull
            val rows = vm.categoryDailyStats.value
            val groups = rows.groupBy { it.category }
                .mapValues { (_, v) -> v.map { it.day to it.maxWeight } }
            val dates = rows.map { it.day }.distinct().sorted()
            renderMultiLineChart(binding.lineChartCategoryWeight, groups, dates, !isCategoryWeightFull)
        }
        binding.lineChartCategorySets.setOnClickListener {
            isCategorySetsFull = !isCategorySetsFull
            val rows = vm.categoryDailyStats.value
            val groups = rows.groupBy { it.category }
                .mapValues { (_, v) -> v.map { it.day to it.totalSets.toFloat() } }
            val dates = rows.map { it.day }.distinct().sorted()
            renderMultiLineChart(binding.lineChartCategorySets, groups, dates, !isCategorySetsFull)
        }
        binding.lineChartOneRM.setOnClickListener {
            isOneRmFull = !isOneRmFull
            val rows = vm.topExerciseOneRM.value
            val groups = rows.groupBy { it.exerciseName }
                .mapValues { (_, v) -> v.map { it.day to it.estimated1RM } }
            val dates = rows.map { it.day }.distinct().sorted()
            renderMultiLineChart(binding.lineChartOneRM, groups, dates, !isOneRmFull)
        }
    }

    private fun renderLineChart(
        chart: LineChart,
        points: List<ChartPoint>,
        compact: Boolean,
        xLabels: List<String>? = null
    ) {
        val visible = if (compact) points.takeLast(7) else points
        val entries = visible.map { Entry(it.x, it.y) }
        if (entries.isEmpty()) return

        val mainDataSet = LineDataSet(entries, "").apply {
            setDrawValues(false)
            setDrawCircles(true)
            circleRadius = 4f
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val maxEntry = entries.maxByOrNull { it.y }
        val maxDataSet = maxEntry?.let {
            LineDataSet(listOf(it), "").apply {
                lineWidth = 0f
                setDrawCircles(true)
                setDrawCircleHole(false)
                circleRadius = 7f
                setCircleColor(Color.RED)
                setDrawValues(true)
                valueTextSize = 10f
                valueTextColor = Color.RED
            }
        }

        val avg = entries.map { it.y }.average().toFloat()
        chart.axisLeft.removeAllLimitLines()
        chart.axisLeft.addLimitLine(
            LimitLine(avg, "avg ${avg.toInt()}").apply {
                lineColor = Color.GRAY
                lineWidth = 1f
                enableDashedLine(10f, 5f, 0f)
                textColor = Color.WHITE
                textSize = 9f
            }
        )

        chart.data = LineData(listOfNotNull(mainDataSet, maxDataSet))
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = Color.WHITE
            if (xLabels != null) {
                valueFormatter = IndexAxisValueFormatter(xLabels)
                labelRotationAngle = -25f
                labelCount = xLabels.size
            }
        }
        chart.axisLeft.apply {
            axisMinimum = 0f
            textColor = Color.WHITE
        }

        if (!compact) {
            chart.setVisibleXRangeMaximum(7f)
            chart.moveViewToX(entries.last().x)
        }
        chart.invalidate()
    }

    private fun renderMultiLineChart(
        chart: LineChart,
        groups: Map<String, List<Pair<String, Float>>>,
        allDates: List<String>,
        compact: Boolean
    ) {
        val visibleDates = if (compact) allDates.takeLast(7) else allDates
        val dateIndex = visibleDates.withIndex().associate { (i, d) -> d to i.toFloat() }
        val shortLabels = visibleDates.map { it.substring(5) }

        val dataSets = groups.entries.mapIndexed { idx, (groupName, points) ->
            val entries = points
                .filter { (day, _) -> day in dateIndex }
                .map { (day, value) -> Entry(dateIndex[day]!!, value) }
                .sortedBy { it.x }
            if (entries.isEmpty()) return@mapIndexed null

            val color = lineColors[idx % lineColors.size]
            LineDataSet(entries, groupName).apply {
                this.color = color
                setCircleColor(color)
                lineWidth = 2f
                circleRadius = 3f
                mode = LineDataSet.Mode.LINEAR
                setDrawValues(false)
                setDrawCircleHole(false)
            }
        }.filterNotNull()

        if (dataSets.isEmpty()) return

        chart.data = LineData(dataSets)
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.legend.textColor = Color.WHITE
        chart.legend.textSize = 10f
        chart.axisRight.isEnabled = false
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = Color.WHITE
            valueFormatter = IndexAxisValueFormatter(shortLabels)
            labelRotationAngle = -25f
            labelCount = shortLabels.size
        }
        chart.axisLeft.apply {
            axisMinimum = 0f
            textColor = Color.WHITE
        }
        chart.setVisibleXRangeMaximum(7f)
        chart.moveViewToX(visibleDates.size.toFloat())
        chart.invalidate()
    }
}
