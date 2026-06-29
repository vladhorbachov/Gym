package com.gymshark.utils.view.calendarview.adapter

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.databinding.ItemDayBinding
import com.gymshark.domain.models.TrainingCalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class DaysAdapter(
    private val typesProvider: (LocalDate) -> List<String>,
    private val onClick: (TrainingCalendarDay, List<String>) -> Unit,
    private val onTrainingDropped: (sourceDate: LocalDate, targetDate: LocalDate) -> Unit
) : ListAdapter<TrainingCalendarDay, DaysAdapter.DayVH>(DiffCallback()) {

    var plannedDays: Set<DayOfWeek> = emptySet()
        private set

    private var selectedDate: LocalDate? = null

    private val today: LocalDate
        get() = LocalDate.now()

    fun updatePlannedDays(days: Set<DayOfWeek>) {
        if (plannedDays == days) return

        val oldDays = plannedDays
        plannedDays = days

        currentList.forEachIndexed { index, item ->
            val wasPlanned = oldDays.contains(item.date.dayOfWeek)
            val isPlannedNow = plannedDays.contains(item.date.dayOfWeek)

            if (wasPlanned != isPlannedNow) {
                notifyItemChanged(index, PAYLOAD_STYLE_ONLY)
            }
        }
    }

    fun refreshStyles() {
        currentList.indices.forEach { index ->
            notifyItemChanged(index, PAYLOAD_STYLE_ONLY)
        }
    }

    fun updateSelectedDate(date: LocalDate?) {
        if (selectedDate == date) return
        val oldDate = selectedDate
        selectedDate = date

        currentList.forEachIndexed { index, item ->
            if (item.date == oldDate || item.date == selectedDate) {
                notifyItemChanged(index, PAYLOAD_STYLE_ONLY)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val binding = ItemDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayVH(binding)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        holder.bind(getItem(position), fullBind = true)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_STYLE_ONLY)) {
            holder.bind(getItem(position), fullBind = false)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    inner class DayVH(
        private val binding: ItemDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrainingCalendarDay, fullBind: Boolean) {
            val locale = Locale.getDefault()
            val date = item.date
            val typesForDay = typesProvider(date)

            if (fullBind) {
                binding.dayNumber.text = date.dayOfMonth.toString()
                binding.dayName.text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

                binding.root.alpha = 1f
                binding.root.translationX = 0f
                binding.root.translationY = 0f
                binding.root.isEnabled = true

                binding.root.setOnClickListener { view ->
                    val currentTypes = typesProvider(item.date)

                    view.animate().cancel()
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.isEnabled = false

                    view.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(110L)
                        .withEndAction {
                            view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(110L)
                                .withEndAction {
                                    view.isEnabled = true
                                    onClick(item, currentTypes)
                                }
                                .start()
                        }
                        .start()
                }

                binding.root.setOnLongClickListener { view ->
                    val currentTypes = typesProvider(item.date)
                    if (!canStartDrag(item, currentTypes)) return@setOnLongClickListener false

                    val payload = CalendarDragPayload(sourceDate = item.date)
                    val clip = ClipData.newPlainText(DRAG_LABEL, item.date.toString())
                    val started = view.startDragAndDrop(
                        clip,
                        TrainingOutlineShadow(view),
                        payload,
                        0
                    )
                    if (!started) resetDragTarget(view)
                    true
                }

                binding.root.setOnDragListener { view, event ->
                    handleDrag(view, event, item)
                }
            }

            applyVisualState(item, typesForDay)
        }

        private fun applyVisualState(item: TrainingCalendarDay, typesForDay: List<String>) {
            val date = item.date
            val isToday = date == today
            val hasCompletedTrain = item.train != null
            val isPlanned = plannedDays.contains(date.dayOfWeek)
            val hasPlannedTrain = isPlanned && typesForDay.isNotEmpty()
            binding.root.foreground = if (date == selectedDate) {
                ContextCompat.getDrawable(binding.root.context, R.drawable.day_selected_foreground)
            } else {
                null
            }

            when {
                isToday && (hasCompletedTrain || hasPlannedTrain) -> applyTodayWithTraining()
                isToday -> applyToday()
                hasCompletedTrain -> applyCompleted()
                hasPlannedTrain -> applyPlanned()
                else -> applyDefault()
            }
        }

        private fun applyToday() {
            binding.root.setBackgroundResource(R.drawable.day_today)
            binding.dayNumber.setTextColor(getColor(R.color.trainingOnAction))
            binding.dayName.setTextColor(getColor(R.color.trainingOnAction))
        }

        private fun applyTodayWithTraining() {
            binding.root.setBackgroundResource(R.drawable.day_today_planned)
            binding.dayNumber.setTextColor(getColor(R.color.trainingOnAction))
            binding.dayName.setTextColor(getColor(R.color.trainingOnAction))
        }

        private fun applyCompleted() {
            binding.root.setBackgroundResource(R.drawable.day_completed)
            binding.dayNumber.setTextColor(getColor(R.color.text_primary))
            binding.dayName.setTextColor(getColor(R.color.text_primary))
        }

        private fun applyPlanned() {
            binding.root.setBackgroundResource(R.drawable.day_planned)
            binding.dayNumber.setTextColor(getColor(R.color.text_primary))
            binding.dayName.setTextColor(getColor(R.color.text_secondary))
        }

        private fun applyDefault() {
            binding.root.setBackgroundResource(R.drawable.day_default)
            binding.dayNumber.setTextColor(getColor(R.color.text_secondary))
            binding.dayName.setTextColor(getColor(R.color.text_secondary))
        }

        private fun getColor(id: Int): Int {
            return ContextCompat.getColor(binding.root.context, id)
        }

        private fun canStartDrag(
            item: TrainingCalendarDay,
            typesForDay: List<String>
        ): Boolean {
            return !item.date.isBefore(today) &&
                typesForDay.isNotEmpty()
        }

        private fun canDropOn(item: TrainingCalendarDay): Boolean {
            return item.train == null &&
                !item.date.isBefore(today) &&
                typesProvider(item.date).isEmpty()
        }

        private fun handleDrag(
            view: View,
            event: DragEvent,
            target: TrainingCalendarDay
        ): Boolean {
            val payload = event.localState as? CalendarDragPayload ?: return false

            return when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> {
                    if (canDropOn(target) && payload.sourceDate != target.date) {
                        view.animate()
                            .scaleX(1.08f)
                            .scaleY(1.08f)
                            .alpha(0.86f)
                            .setDuration(100L)
                            .start()
                    }
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    resetDragTarget(view)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    resetDragTarget(view)
                    if (canDropOn(target) && payload.sourceDate != target.date) {
                        onTrainingDropped(payload.sourceDate, target.date)
                        true
                    } else {
                        false
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    resetDragTarget(view)
                    true
                }
                else -> true
            }
        }

        private fun resetDragTarget(view: View) {
            view.animate().cancel()
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(120L)
                .start()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TrainingCalendarDay>() {
        override fun areItemsTheSame(
            oldItem: TrainingCalendarDay,
            newItem: TrainingCalendarDay
        ): Boolean = oldItem.date == newItem.date

        override fun areContentsTheSame(
            oldItem: TrainingCalendarDay,
            newItem: TrainingCalendarDay
        ): Boolean = oldItem == newItem
    }

    private companion object {
        const val PAYLOAD_STYLE_ONLY = "payload_style_only"
        const val DRAG_LABEL = "training_calendar_day"
    }

    private data class CalendarDragPayload(
        val sourceDate: LocalDate
    )

    private class TrainingOutlineShadow(
        private val sourceView: View
    ) : View.DragShadowBuilder(sourceView) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = sourceView.resources.displayMetrics.density * 2f
            color = ContextCompat.getColor(sourceView.context, R.color.trainingAction)
        }

        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            size.set(sourceView.width, sourceView.height)
            touch.set(sourceView.width / 2, sourceView.height / 2)
        }

        override fun onDrawShadow(canvas: Canvas) {
            val inset = paint.strokeWidth / 2f
            val radius = sourceView.resources.displayMetrics.density * 16f
            val rect = RectF(
                inset,
                inset,
                sourceView.width - inset,
                sourceView.height - inset
            )

            canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }
}
