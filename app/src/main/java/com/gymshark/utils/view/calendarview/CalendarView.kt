package com.gymshark.utils.view.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymshark.data.models.TrainingCalendarDay
import com.gymshark.data.models.Train
import com.gymshark.databinding.ViewCalendarBinding
import com.gymshark.utils.view.calendarview.adapter.DaysAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewCalendarBinding.inflate(LayoutInflater.from(context), this)
    private val adapter = DaysAdapter { train ->
        onTrainSelected?.invoke(train)
    }

    private var currentCalendar = Calendar.getInstance()
    private var trains: List<Train> = emptyList()
    private var trainingDays: Set<Int> = emptySet()
    var onTrainSelected: ((Train?) -> Unit)? = null

    init {
        orientation = VERTICAL
        binding.daysRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.daysRecycler.adapter = adapter

        binding.arrowLeft.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateMonth()
        }

        binding.arrowRight.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateMonth()
        }

        updateMonth()
    }

    fun setTrains(list: List<Train>) {
        trains = list
        updateMonth()
    }
    fun setTrainingDays(days: Set<Int>) {
        adapter.trainingDays = days
    }

    private fun updateMonth() {
        val dateFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        binding.monthText.text = dateFormat.format(currentCalendar.time)
            .replaceFirstChar { it.uppercase() }

        val days = ArrayList<TrainingCalendarDay>()
        val cal = currentCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val month = cal.get(Calendar.MONTH)

        while (cal.get(Calendar.MONTH) == month) {
            val time = cal.timeInMillis
            val train = trains.find {
                val tCal = Calendar.getInstance().apply { timeInMillis = it.date }
                tCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                        tCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                        tCal.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
            }
            days.add(TrainingCalendarDay(date = time, train = train))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        adapter.submitList(days) {
            val today = Calendar.getInstance()
            if (today.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
            ) {
                val todayPosition = today.get(Calendar.DAY_OF_MONTH) - 1
                binding.daysRecycler.post {
                    centerTodayInRecycler(todayPosition)
                }
            }
        }
    }

    private fun centerTodayInRecycler(todayPosition: Int) {
        val layoutManager = binding.daysRecycler.layoutManager as LinearLayoutManager
        val recyclerWidth = binding.daysRecycler.width

        val firstChild = binding.daysRecycler.getChildAt(0)
            ?: binding.daysRecycler.findViewHolderForAdapterPosition(0)?.itemView

        if (firstChild != null && firstChild.width > 0) {
            val itemWidth = firstChild.width
            val offset = recyclerWidth / 2 - itemWidth / 2
            layoutManager.scrollToPositionWithOffset(todayPosition, offset)
        } else {
            binding.daysRecycler.scrollToPosition(todayPosition)
        }
    }
}
