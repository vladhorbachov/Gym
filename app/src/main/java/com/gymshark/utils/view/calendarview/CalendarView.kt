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
import java.time.DayOfWeek
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
    private var todayDispatched = false
    private var isMonthAnimating = false
    private var lastAutoSelectKey: String? = null
    var onDayClick: ((LocalDate, Train?, List<String>) -> Unit)? = null

    init {
        orientation = VERTICAL
        binding.daysRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.daysRecycler.adapter = adapter

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
        trains = list
        updateMonth()
        tryAutoSelectIfReady()
    }

    fun setTrainingDays(days: Set<DayOfWeek>) {
        plannedDays = days
        adapter.plannedDays = days
        updateMonth()
        tryAutoSelectIfReady()
    }

    fun setTrainingSlots(slots: List<DaySlot>) {
        trainingSlots = slots
        updateMonth()
        tryAutoSelectIfReady()
    }
    private fun tryAutoSelectIfReady() {
        val today = LocalDate.now()

        if (today.year != currentMonth.year || today.month != currentMonth.month) return

        if (trainingSlots.isEmpty()) return
        if (plannedDays.isEmpty()) return

        val types = typesForDate(today)
        if (types.isEmpty()) return

        val key = "${today}|${types.joinToString(",")}|slots=${trainingSlots.size}|planned=${plannedDays.size}"
        if (lastAutoSelectKey == key) return
        lastAutoSelectKey = key

        post {
            onDayClick?.invoke(today, trains.find { it.localDate == today }, types)
            animateTodayCell(today)
        }
    }

    private fun animateTodayCell(today: LocalDate) {
        binding.daysRecycler.postDelayed({
            val pos = today.dayOfMonth - 1
            val vh = binding.daysRecycler.findViewHolderForAdapterPosition(pos) ?: return@postDelayed
            vh.itemView.animate()
                .scaleX(1.06f).scaleY(1.06f)
                .setDuration(300)
                .withEndAction {
                    vh.itemView.animate().scaleX(1f).scaleY(1f).setDuration(250).start()
                }
                .start()
        }, 80)
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
        isMonthAnimating = true

        binding.daysRecycler.animate()
            .alpha(0f)
            .translationX(80f)
            .setDuration(140)
            .withEndAction {

                adapter.submitList(days) {

                    binding.daysRecycler.translationX = -80f

                    binding.daysRecycler.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(180)
                        .withEndAction {
                            isMonthAnimating = false
                        }
                        .start()

                    val today = LocalDate.now()
                    if (today.year == currentMonth.year && today.month == currentMonth.month) {
                        val pos = today.dayOfMonth - 1
                        binding.daysRecycler.post { binding.daysRecycler.scrollToPosition(pos) }
                    }
                }


            }
            .start()



    }

    private fun trySelectToday() {

        if (todayDispatched) return
        if (trainingSlots.isEmpty()) return
        if (plannedDays.isEmpty()) return

        val today = LocalDate.now()
        val types = typesForDate(today)

        if (types.isEmpty()) return

        todayDispatched = true
        post { onDayClick?.invoke(today, trains.find { it.localDate == today }, types) }
        binding.daysRecycler.postDelayed({
            val pos = today.dayOfMonth - 1
            val vh = binding.daysRecycler.findViewHolderForAdapterPosition(pos)


            vh?.itemView?.animate()
                ?.scaleX(1.06f)
                ?.scaleY(1.06f)
                ?.setDuration(300)
                ?.withEndAction {
                    vh.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(250)
                        .start()
                }
                ?.start()
        }, 80)


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
