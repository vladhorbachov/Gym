package com.gymshark.utils.view.calendarview.adapter

import android.view.LayoutInflater
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
    private val onClick: (TrainingCalendarDay, List<String>) -> Unit
) : ListAdapter<TrainingCalendarDay, DaysAdapter.DayVH>(DiffCallback()) {

    var plannedDays: Set<DayOfWeek> = emptySet()
        private set

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
                                    onClick(item, typesForDay)
                                }
                                .start()
                        }
                        .start()
                }
            }

            applyVisualState(item, typesForDay)
        }

        private fun applyVisualState(item: TrainingCalendarDay, typesForDay: List<String>) {
            val date = item.date
            val isToday = date == today
            val hasCompletedTrain = item.train != null
            val isPlanned = plannedDays.contains(date.dayOfWeek)

            when {
                isToday -> applyToday()
                hasCompletedTrain -> applyCompleted()
                isPlanned && typesForDay.isNotEmpty() -> applyPlanned()
                else -> applyDefault()
            }
        }

        private fun applyToday() {
            binding.root.setBackgroundResource(R.drawable.day_today)
            binding.dayNumber.setTextColor(getColor(R.color.text_primary))
            binding.dayName.setTextColor(getColor(R.color.text_primary))
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
    }
}