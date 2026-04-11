package com.gymshark.utils.view.calendarview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymshark.databinding.ViewCalendarBinding
import com.gymshark.domain.models.DaySlot
import com.gymshark.domain.models.Train
import com.gymshark.domain.models.TrainingCalendarDay
import com.gymshark.utils.view.calendarview.adapter.DaysAdapter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewCalendarBinding.inflate(LayoutInflater.from(context), this)

    private var trainingSlots: List<DaySlot> = emptyList()
    private var trains: List<Train> = emptyList()
    private var plannedDays: Set<DayOfWeek> = emptySet()
    private var currentMonth: YearMonth = YearMonth.now()

    private var isMonthAnimating = false
    private var lastAutoSelectKey: String? = null
    private var pendingAutoSelectToday = false
    private var pendingMonthRefresh = false

    private val adapter = DaysAdapter(
        typesProvider = ::typesForDate,
        onClick = { day, types ->
            onDayClick?.invoke(day.date, day.train, types)
        }
    )

    var onDayClick: ((LocalDate, Train?, List<String>) -> Unit)? = null

    init {
        orientation = VERTICAL

        binding.daysRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.daysRecycler.adapter = adapter
        binding.daysRecycler.itemAnimator = null

        binding.arrowLeft.setOnClickListener {
            if (isMonthAnimating) return@setOnClickListener
            currentMonth = currentMonth.minusMonths(1)
            lastAutoSelectKey = null
            updateMonth()
        }

        binding.arrowRight.setOnClickListener {
            if (isMonthAnimating) return@setOnClickListener
            currentMonth = currentMonth.plusMonths(1)
            lastAutoSelectKey = null
            updateMonth()
        }

        updateMonth()
    }

    fun setTrains(list: List<Train>) {
        if (trains == list) return
        trains = list
        requestAutoSelectToday()
        requestMonthRefresh()
    }

    fun setTrainingDays(days: Set<DayOfWeek>) {
        if (plannedDays == days) return
        plannedDays = days
        adapter.updatePlannedDays(days)
        requestAutoSelectToday()
        requestMonthRefresh()
    }

    fun setTrainingSlots(slots: List<DaySlot>) {
        if (trainingSlots == slots) return
        trainingSlots = slots
        requestAutoSelectToday()
        requestMonthRefresh()
    }

    private fun requestAutoSelectToday() {
        pendingAutoSelectToday = true
    }

    private fun requestMonthRefresh() {
        if (pendingMonthRefresh) return
        pendingMonthRefresh = true

        post {
            pendingMonthRefresh = false
            updateMonth()
        }
    }

    private fun updateMonth() {
        val locale = Locale.getDefault()
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
        binding.monthText.text =
            monthName.replaceFirstChar { it.titlecase(locale) } + " ${currentMonth.year}"

        val days = buildMonthDays()
        val today = LocalDate.now()
        val isCurrentMonth = today.year == currentMonth.year && today.month == currentMonth.month

        isMonthAnimating = true

        binding.daysRecycler.animate().cancel()
        binding.daysRecycler.alpha = 0f

        adapter.submitList(days) {
            binding.daysRecycler.post {
                val layoutManager = binding.daysRecycler.layoutManager as LinearLayoutManager

                if (isCurrentMonth) {
                    val pos = today.dayOfMonth - 1
                    layoutManager.scrollToPositionWithOffset(pos, 0)
                } else {
                    layoutManager.scrollToPositionWithOffset(0, 0)
                }

                binding.daysRecycler.animate().cancel()
                binding.daysRecycler.alpha = 1f
                isMonthAnimating = false
                maybeAutoSelectToday()
            }
        }
    }

    private fun buildMonthDays(): List<TrainingCalendarDay> {
        val length = currentMonth.lengthOfMonth()

        return List(length) { index ->
            val date = currentMonth.atDay(index + 1)
            val train = trains.firstOrNull { it.localDate == date }
            TrainingCalendarDay(date = date, train = train)
        }
    }

    private fun maybeAutoSelectToday() {
        if (!pendingAutoSelectToday) return

        val today = LocalDate.now()

        if (today.year != currentMonth.year || today.month != currentMonth.month) {
            pendingAutoSelectToday = false
            return
        }

        if (trainingSlots.isEmpty()) return
        if (plannedDays.isEmpty()) return

        val types = typesForDate(today)
        if (types.isEmpty()) return

        val key = buildString {
            append(today)
            append("|")
            append(types.joinToString(","))
            append("|slots=")
            append(trainingSlots.size)
            append("|planned=")
            append(plannedDays.size)
            append("|trains=")
            append(trains.size)
        }

        if (lastAutoSelectKey == key) {
            pendingAutoSelectToday = false
            return
        }

        lastAutoSelectKey = key
        pendingAutoSelectToday = false

        animateTodayCell(today) {
            onDayClick?.invoke(today, trains.firstOrNull { it.localDate == today }, types)
        }
    }

    private fun animateTodayCell(today: LocalDate, onEnd: (() -> Unit)? = null) {
        val position = today.dayOfMonth - 1

        binding.daysRecycler.post {
            if (isMonthAnimating) return@post

            val viewHolder =
                binding.daysRecycler.findViewHolderForAdapterPosition(position) ?: run {
                    onEnd?.invoke()
                    return@post
                }

            val itemView = viewHolder.itemView
            itemView.animate().cancel()
            itemView.clearAnimation()
            itemView.scaleX = 1f
            itemView.scaleY = 1f

            itemView.animate()
                .scaleX(1.04f)
                .scaleY(1.04f)
                .setDuration(120L)
                .withEndAction {
                    itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120L)
                        .withEndAction {
                            onEnd?.invoke()
                        }
                        .start()
                }
                .start()
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