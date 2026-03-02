package com.gymshark.utils.view.calendarview.adapter

import android.graphics.Color
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
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val today: LocalDate = LocalDate.now()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayVH(binding)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayVH(private val binding: ItemDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrainingCalendarDay) {

            val locale = Locale.getDefault()
            binding.dayNumber.text = item.date.dayOfMonth.toString()
            binding.dayName.text = item.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

            val isToday = item.date == today
            val hasCompletedTrain = item.train != null
            val isPlanned = plannedDays.contains(item.date.dayOfWeek)
            val typesForDay = typesProvider(item.date)

            when {
                isToday -> applyToday()
                hasCompletedTrain -> applyCompleted()
                isPlanned && typesForDay.isNotEmpty() -> applyPlanned()
                else -> applyDefault()
            }

            binding.root.setOnClickListener {

                it.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(70)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(180)
                            .start()

                        onClick(item, typesForDay)
                    }
                    .start()
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

        private fun getColor(id: Int) =
            ContextCompat.getColor(binding.root.context, id)

    }

    class DiffCallback : DiffUtil.ItemCallback<TrainingCalendarDay>() {
        override fun areItemsTheSame(o: TrainingCalendarDay, n: TrainingCalendarDay) =
            o.date == n.date

        override fun areContentsTheSame(o: TrainingCalendarDay, n: TrainingCalendarDay) =
            o == n
    }
}

