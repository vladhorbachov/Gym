package com.gymshark.utils.view.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymshark.domain.models.DaySlot
import com.gymshark.domain.models.Train
import com.gymshark.domain.models.TrainingCalendarDay
import com.gymshark.databinding.ViewCalendarBinding
import com.gymshark.utils.view.calendarview.adapter.DaysAdapter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class CalendarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewCalendarBinding.inflate(LayoutInflater.from(context), this)
    private var trainingSlots: List<DaySlot> = emptyList()
    private val adapter = DaysAdapter(
        typesProvider = { date -> typesForDate(date) }
    ) { day, types ->
        onDayClick?.invoke(day.date, day.train, types)
    }
    private var currentMonth: YearMonth = YearMonth.now()
    private var trains: List<Train> = emptyList()
    private var plannedDays: Set<java.time.DayOfWeek> = emptySet()

    var onDayClick: ((LocalDate, Train?, List<String>) -> Unit)? = null

    init {
        orientation = VERTICAL
        binding.daysRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.daysRecycler.adapter = adapter

        binding.arrowLeft.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateMonth()
        }
        binding.arrowRight.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateMonth()
        }
        updateMonth()
    }

    fun setTrains(list: List<Train>) {
        trains = list
        updateMonth()
    }

    fun setTrainingDays(days: Set<java.time.DayOfWeek>) {
        plannedDays = days
        adapter.plannedDays = days
        updateMonth()
    }

    fun setTrainingSlots(slots: List<DaySlot>) {
        trainingSlots = slots
        updateMonth()
    }

    private fun updateMonth() {
        val locale = Locale.getDefault()
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
        binding.monthText.text =
            monthName.replaceFirstChar { it.titlecase(locale) } + " ${currentMonth.year}"

        val days = buildList {
            val length = currentMonth.lengthOfMonth()
            for (d in 1..length) {
                val date = currentMonth.atDay(d)
                val train = trains.find { it.localDate == date }
                add(TrainingCalendarDay(date = date, train = train))
            }
        }
        adapter.submitList(days) {
            val today = LocalDate.now()
            if (today.year == currentMonth.year && today.month == currentMonth.month) {
                val pos = today.dayOfMonth - 1
                binding.daysRecycler.post { binding.daysRecycler.scrollToPosition(pos) }
            }
        }
    }

    private fun typesForDate(date: LocalDate): List<String> {
        val dayOfWeek = date.dayOfWeek

        val slotsForDay = trainingSlots
            .filter { it.day == dayOfWeek }
            .sortedBy { it.id }

        if (slotsForDay.isEmpty()) return emptyList()

        val today = LocalDate.now()

        val firstPlannedDate = today.with(TemporalAdjusters.nextOrSame(dayOfWeek))

        if (date.isBefore(firstPlannedDate)) return emptyList()

        val weeksBetween = ChronoUnit.WEEKS.between(firstPlannedDate, date)
        val index = (weeksBetween % slotsForDay.size).toInt()

        return slotsForDay[index].types
    }
}
