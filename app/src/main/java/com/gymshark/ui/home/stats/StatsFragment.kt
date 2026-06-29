package com.gymshark.ui.home.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.gymshark.R
import com.gymshark.databinding.FragmentStatsBinding
import com.gymshark.databinding.ItemProgressInsightBinding
import com.gymshark.databinding.ItemProgressLineChartBinding
import com.gymshark.databinding.ItemProgressMetricBinding
import com.gymshark.domain.models.CategoryStatRow
import com.gymshark.domain.models.DailyStatsUi
import com.gymshark.domain.models.MoodDayUi
import com.gymshark.domain.models.MoodUi
import com.gymshark.ui.home.stats.viewmodel.StatsViewModel
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

class StatsFragment : BaseFragment<FragmentStatsBinding>(FragmentStatsBinding::inflate) {

    private val vm: StatsViewModel by activityViewModel()

    private var range = Range.DAYS_30
    private var dailyStats: List<DailyStatsUi> = emptyList()
    private var durationPoints: List<ChartPoint> = emptyList()
    private var caloriesPoints: List<ChartPoint> = emptyList()
    private var bpmPoints: List<ChartPoint> = emptyList()
    private var weightPoints: List<ChartPoint> = emptyList()
    private var categoryStats: List<CategoryStatRow> = emptyList()
    private var moodDays: List<MoodDayUi> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyInsets()
        setupStaticText()
        setupRangeSelector()
        playIntro()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.dailyStats.collect { dailyStats = it.sortedBy { row -> row.day }; renderAll() } }
                launch { vm.durationProgress.collect { durationPoints = it; renderAll() } }
                launch { vm.caloriesProgress.collect { caloriesPoints = it; renderAll() } }
                launch { vm.bpmProgress.collect { bpmPoints = it; renderAll() } }
                launch { vm.weightProgress.collect { weightPoints = it; renderAll() } }
                launch { vm.categoryStats.collect { categoryStats = it; renderAll() } }
                launch { vm.moodTimeline.collect { moodDays = it; renderAll() } }
            }
        }
    }

    private fun setupStaticText() = with(binding) {
        setupChartCard(
            durationCard,
            title = "Training duration",
            emptyTitle = "No workouts yet",
            emptyMessage = "Complete a workout to see your duration trend.",
            emptyIcon = "t"
        )
        setupChartCard(
            caloriesCard,
            title = "Calories burned",
            emptyTitle = "Calories will appear here",
            emptyMessage = "Tracked sessions will build a clearer energy trend.",
            emptyIcon = "c"
        )
        setupChartCard(
            bpmCard,
            title = "Avg BPM trend",
            emptyTitle = "Heart rate data will appear",
            emptyMessage = "Heart rate data will appear after tracked workouts.",
            emptyIcon = "h"
        )
        setupChartCard(
            weightCard,
            title = "Body weight",
            emptyTitle = "Add body weight entries",
            emptyMessage = "Your body metric trend becomes useful after a few check-ins.",
            emptyIcon = "w"
        )
    }

    private fun setupRangeSelector() = with(binding) {
        rangeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            range = when (checkedId) {
                R.id.range7 -> Range.DAYS_7
                R.id.range3m -> Range.MONTHS_3
                R.id.range1y -> Range.YEAR_1
                R.id.rangeAll -> Range.ALL
                else -> Range.DAYS_30
            }
            renderAll()
        }
    }

    private fun renderAll() {
        if (_binding == null) return
        renderSummary()
        renderCharts()
        renderStrength()
        renderInsights()
        renderMood()
    }

    private fun renderSummary() = with(binding) {
        val current = ranged(dailyStats)
        val previous = previousWindow(dailyStats)
        val totalMinutes = current.sumOf { it.totalMinutes }
        val previousMinutes = previous.sumOf { it.totalMinutes }
        val calories = current.sumOf { it.calories }
        val previousCalories = previous.sumOf { it.calories }
        val avgBpm = current.mapNotNull { it.avgBpm }.averageOrNull()
        val previousBpm = previous.mapNotNull { it.avgBpm }.averageOrNull()

        bindMetric(cardTime, "Total time", formatDuration(totalMinutes), deltaText(totalMinutes.toFloat(), previousMinutes.toFloat()), "t")
        bindMetric(cardWorkouts, "Workouts", current.size.toString(), deltaText(current.size.toFloat(), previous.size.toFloat()), "w")
        bindMetric(cardCalories, "Calories", calories.toString(), deltaText(calories.toFloat(), previousCalories.toFloat()), "c")
        bindMetric(cardBpm, "Avg BPM", avgBpm?.roundToInt()?.toString() ?: "No data", avgBpm?.let { deltaText(it.toFloat(), previousBpm?.toFloat()) } ?: "Track HR to compare", "h")
    }

    private fun renderCharts() = with(binding) {
        renderLine(
            card = durationCard,
            points = ranged(durationPoints),
            color = color(R.color.trainingAction),
            meta = metricMeta(ranged(durationPoints), suffix = "min avg"),
            averageLine = true
        )
        renderLine(
            card = caloriesCard,
            points = ranged(caloriesPoints).filterNot { it.y == 0f && ranged(caloriesPoints).any { p -> p.y > 0f } },
            color = color(R.color.chartAmber),
            meta = metricMeta(ranged(caloriesPoints), suffix = "kcal avg"),
            averageLine = true
        )
        renderLine(
            card = bpmCard,
            points = ranged(bpmPoints),
            color = color(R.color.chartRose),
            meta = metricMeta(ranged(bpmPoints), suffix = "bpm avg"),
            averageLine = false
        )
        renderLine(
            card = weightCard,
            points = ranged(weightPoints),
            color = color(R.color.trainingStatus),
            meta = weightMeta(ranged(weightPoints)),
            averageLine = false
        )
    }

    private fun renderStrength(): Unit = with(binding) {
        val rows = categoryStats
            .filter { it.maxWeight > 0f }
            .sortedByDescending { it.maxWeight }
            .take(5)

        strengthEmpty.isVisible = rows.isEmpty()
        barStrength.isVisible = rows.isNotEmpty()

        if (rows.isEmpty()) {
            barStrength.clear()
            return
        }

        val entries = rows.mapIndexed { index, row -> BarEntry(index.toFloat(), row.maxWeight) }
        val dataSet = BarDataSet(entries, "").apply {
            color = color(R.color.trainingAction)
            valueTextColor = color(R.color.textPrimary)
            valueTextSize = 10f
            setDrawValues(true)
        }

        barStrength.apply {
            data = BarData(dataSet).apply { barWidth = 0.48f }
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setFitBars(true)
            animateY(650)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(rows.map { it.category.compactCategory() })
                textColor = color(R.color.textSecondary)
                setDrawGridLines(false)
                granularity = 1f
            }
            axisLeft.apply {
                textColor = color(R.color.textSecondary)
                gridColor = color(R.color.chartGrid)
                axisMinimum = 0f
                setDrawAxisLine(false)
            }
            axisRight.isEnabled = false
            invalidate()
        }
    }

    private fun renderInsights() = with(binding) {
        val current = ranged(dailyStats)
        val longest = current.maxByOrNull { it.totalMinutes }
        val bestDay = current.maxByOrNull { it.calories + it.totalMinutes.toInt() }
        val currentWeights = ranged(weightPoints)

        bindInsight(insightLongest, "Longest workout", longest?.let { "${it.totalMinutes} min on ${it.day.toShortDate()}" } ?: "Complete a workout to unlock", "l")
        bindInsight(insightBestDay, "Best training day", bestDay?.let { "${it.day.toShortDate()} - ${it.calories} kcal" } ?: "No completed sessions yet", "b")
        bindInsight(insightStreak, "Current streak", "${current.size} tracked days in this range", "s")
        bindInsight(insightWeight, "Weight change", weightMeta(currentWeights).ifBlank { "Add weight entries to compare" }, "w")
    }

    private fun renderMood() = with(binding) {
        moodBars.removeAllViews()
        if (moodDays.isEmpty()) {
            tvRecoverySummary.text = "Log workout feeling after sessions to spot recovery patterns."
            addMoodBar(MoodUi.NEUTRAL, 1)
            return
        }

        val counts = moodDays.groupingBy { it.mood }.eachCount()
        val top = counts.maxByOrNull { it.value }?.key ?: MoodUi.NEUTRAL
        tvRecoverySummary.text = "This week trends ${top.label().lowercase()} across ${moodDays.size} tracked sessions."

        MoodUi.values().forEach { mood ->
            addMoodBar(mood, counts[mood] ?: 0)
        }
    }

    private fun renderLine(
        card: ItemProgressLineChartBinding,
        points: List<ChartPoint>,
        color: Int,
        meta: String,
        averageLine: Boolean
    ) {
        card.tvChartMeta.text = meta
        card.emptyState.isVisible = points.isEmpty()
        card.lineChart.isVisible = points.isNotEmpty()

        if (points.isEmpty()) {
            card.lineChart.clear()
            return
        }

        val entries = points.mapIndexed { index, point -> Entry(index.toFloat(), point.y) }
        val labels = points.map { it.label }
        val dataSet = LineDataSet(entries, "").apply {
            this.color = color
            setCircleColor(color)
            setDrawCircleHole(false)
            setDrawCircles(entries.size <= 18)
            circleRadius = 3.5f
            lineWidth = 2.6f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = color
            fillAlpha = 34
        }
        val latest = LineDataSet(listOf(entries.last()), "").apply {
            lineWidth = 0f
            setDrawValues(false)
            setDrawCircles(true)
            setDrawCircleHole(false)
            circleRadius = 6f
            setCircleColor(color(R.color.chartPeak))
        }

        card.lineChart.axisLeft.removeAllLimitLines()
        if (averageLine && entries.size > 1) {
            val avg = entries.map { it.y }.average().toFloat()
            card.lineChart.axisLeft.addLimitLine(
                LimitLine(avg, "avg").apply {
                    lineColor = color(R.color.chartGrid)
                    lineWidth = 1f
                    enableDashedLine(10f, 7f, 0f)
                    textColor = color(R.color.textSecondary)
                    textSize = 9f
                }
            )
        }

        card.lineChart.data = LineData(dataSet, latest)
        styleLineChart(card.lineChart, labels, entries)
    }

    private fun styleLineChart(chart: LineChart, labels: List<String>, entries: List<Entry>) {
        val axisTextColor = color(R.color.textSecondary)
        val gridColor = color(R.color.chartGrid)
        val minY = entries.minOf { it.y }
        val maxY = entries.maxOf { it.y }
        val padding = ((maxY - minY) * 0.2f).coerceAtLeast(1f)

        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawBorders(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setBackgroundColor(Color.TRANSPARENT)
            setNoDataTextColor(axisTextColor)
            setExtraOffsets(4f, 8f, 8f, 6f)
            animateX(520)
        }
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = axisTextColor
            valueFormatter = IndexAxisValueFormatter(labels)
            labelRotationAngle = -25f
            labelCount = labels.size.coerceAtMost(5)
            axisMinimum = -0.2f
            axisMaximum = (labels.lastIndex + 0.2f).coerceAtLeast(0.8f)
        }
        chart.axisLeft.apply {
            axisMinimum = if (minY >= 0f) (minY - padding).coerceAtLeast(0f) else minY - padding
            axisMaximum = maxY + padding
            textColor = axisTextColor
            setGridColor(gridColor)
            setDrawAxisLine(false)
        }
        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    private fun setupChartCard(
        card: ItemProgressLineChartBinding,
        title: String,
        emptyTitle: String,
        emptyMessage: String,
        emptyIcon: String
    ) {
        card.tvChartTitle.text = title
        card.tvEmptyTitle.text = emptyTitle
        card.tvEmptyMessage.text = emptyMessage
        card.tvEmptyIcon.text = emptyIcon
    }

    private fun bindMetric(
        card: ItemProgressMetricBinding,
        title: String,
        value: String,
        delta: String,
        icon: String
    ) {
        card.tvMetricTitle.text = title
        card.tvMetricValue.text = value
        card.tvMetricDelta.text = delta
        card.tvMetricIcon.text = icon
    }

    private fun bindInsight(card: ItemProgressInsightBinding, title: String, value: String, icon: String) {
        card.tvInsightTitle.text = title
        card.tvInsightValue.text = value
        card.tvInsightIcon.text = icon
    }

    private fun addMoodBar(mood: MoodUi, count: Int) {
        val weight = count.coerceAtLeast(1).toFloat()
        val view = TextView(requireContext()).apply {
            text = mood.label()
            gravity = android.view.Gravity.CENTER
            textSize = 11f
            setTextColor(color(R.color.textPrimary))
            setBackgroundColor(mood.color())
            alpha = if (count == 0) 0.28f else 0.9f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight).apply {
                marginEnd = 6
            }
        }
        binding.moodBars.addView(view)
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.statsScroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, 0)
            insets
        }
        ViewCompat.requestApplyInsets(binding.statsScroll)
    }

    private fun playIntro() {
        binding.content.alpha = 0f
        binding.content.translationY = 18f
        binding.content.animate().alpha(1f).translationY(0f).setDuration(280L).start()
    }

    private fun <T> ranged(list: List<T>): List<T> =
        range.maxItems?.let { list.takeLast(it) } ?: list

    private fun previousWindow(list: List<DailyStatsUi>): List<DailyStatsUi> {
        val count = range.maxItems ?: return emptyList()
        return list.dropLast(count).takeLast(count)
    }

    private fun metricMeta(points: List<ChartPoint>, suffix: String): String {
        if (points.isEmpty()) return "No data in this range"
        val avg = points.map { it.y }.average().roundToInt()
        val latest = points.last().y.roundToInt()
        return "Latest $latest - $avg $suffix"
    }

    private fun weightMeta(points: List<ChartPoint>): String {
        if (points.isEmpty()) return "No weight data in this range"
        val current = points.last().y
        if (points.size == 1) return "Current ${current.clean()} kg"
        val delta = current - points.first().y
        val sign = if (delta >= 0f) "+" else ""
        return "Current ${current.clean()} kg - $sign${delta.clean()} kg"
    }

    private fun deltaText(current: Float, previous: Float?): String {
        if (previous == null || previous <= 0f) return "No previous period"
        val change = ((current - previous) / previous * 100f).roundToInt()
        val sign = if (change >= 0) "+" else ""
        return "$sign$change% vs last period"
    }

    private fun formatDuration(minutes: Long): String =
        if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"

    private fun List<Int>.averageOrNull(): Double? =
        if (isEmpty()) null else average()

    private fun String.toShortDate(): String =
        if (length >= 10) substring(5).replace("-", ".") else this

    private fun String.compactCategory(): String =
        take(9).replaceFirstChar { it.uppercase() }

    private fun Float.clean(): String =
        if (abs(this - roundToInt()) < 0.05f) roundToInt().toString() else "%.1f".format(this)

    private fun MoodUi.label(): String = when (this) {
        MoodUi.BAD -> "Low"
        MoodUi.NEUTRAL -> "Neutral"
        MoodUi.GOOD -> "Good"
        MoodUi.AMAZING -> "Strong"
    }

    private fun MoodUi.color(): Int = when (this) {
        MoodUi.BAD -> color(R.color.chartRose)
        MoodUi.NEUTRAL -> color(R.color.chartViolet)
        MoodUi.GOOD -> color(R.color.trainingAction)
        MoodUi.AMAZING -> color(R.color.trainingStatus)
    }

    private fun color(resId: Int): Int =
        ContextCompat.getColor(requireContext(), resId)

    private enum class Range(val maxItems: Int?) {
        DAYS_7(7),
        DAYS_30(30),
        MONTHS_3(90),
        YEAR_1(365),
        ALL(null)
    }
}
