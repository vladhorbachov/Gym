package com.gymshark.ui.home.stats

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.gymshark.R
import com.gymshark.domain.models.MoodUi
import com.gymshark.databinding.FragmentStatsBinding
import com.gymshark.ui.home.stats.viewmodel.StatsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/*
* графік прогрес ваги
* середній пульс
* кількість ккал на день
* час тренувань
* почуття після тренування
*
* */
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val vm: StatsViewModel by activityViewModel()
    private var isFullMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        val chart = binding.lineChart

        vm.loadWeightProgress()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.weightProgress.collect { points ->
                if (points.isNotEmpty()) {
                    render(chart, points, lastOnly = !isFullMode)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.dailyStats.collect { list ->
                val today = list.firstOrNull() ?: return@collect

                binding.tvTime.text =
                    "Training time: ${today.totalMinutes} min"

                binding.tvCalories.text =
                    "Calories: ${today.calories}"

                binding.tvBpm.text =
                    today.avgBpm?.let { "Avg BPM: $it" }
                        ?: "Avg BPM: not measured"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.moodTimeline.collect { list ->
                binding.moodContainer.removeAllViews()

                list.forEach { item ->
                    val tv = TextView(requireContext()).apply {
                        text = when (item.mood) {
                            MoodUi.BAD -> "😣"
                            MoodUi.NEUTRAL -> "😐"
                            MoodUi.GOOD -> "🙂"
                            MoodUi.AMAZING -> "🔥"
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
                binding.tvWeeklyMood.text =
                    mood?.let { "This week: $it" } ?: "This week: —"
            }
        }

        chart.setOnClickListener {
            isFullMode = !isFullMode
            render(chart, vm.weightProgress.value, lastOnly = !isFullMode)
        }
    }

    private fun render(
        chart: LineChart,
        points: List<ChartPoint>,
        lastOnly: Boolean
    ) {
        val visible = if (lastOnly) points.takeLast(5) else points

        val entries = visible.map {
            Entry(it.x, it.y)
        }

        val dataSet = LineDataSet(entries, "Weight").apply {
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

        if (!lastOnly) {
            chart.setVisibleXRangeMaximum(7f)
            chart.moveViewToX(entries.last().x)
        }

        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
