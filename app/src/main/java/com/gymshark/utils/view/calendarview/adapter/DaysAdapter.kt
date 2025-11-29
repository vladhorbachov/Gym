package com.gymshark.utils.view.calendarview.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.models.TrainingCalendarDay
import com.gymshark.databinding.ItemDayBinding
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
                isToday -> setBgText("#2196F3", Color.WHITE, Color.WHITE)
                hasCompletedTrain -> setBgText("#4CAF50", Color.WHITE, Color.WHITE)
                isPlanned && typesForDay.isNotEmpty() -> setBgText(
                    "#9C27B0",
                    Color.WHITE,
                    Color.WHITE
                )

                else -> setBgTextTransparent()
            }

            binding.root.setOnClickListener {
                onClick(item, typesForDay)
            }
        }

        private fun setBgText(bgColorHex: String, numColor: Int, nameColor: Int) {
            binding.root.setBackgroundColor(Color.parseColor(bgColorHex))
            binding.dayNumber.setTextColor(numColor)
            binding.dayName.setTextColor(nameColor)
        }

        private fun setBgTextTransparent() {
            binding.root.setBackgroundColor(Color.TRANSPARENT)
            binding.dayNumber.setTextColor(Color.BLACK)
            binding.dayName.setTextColor(Color.GRAY)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TrainingCalendarDay>() {
        override fun areItemsTheSame(o: TrainingCalendarDay, n: TrainingCalendarDay) =
            o.date == n.date

        override fun areContentsTheSame(o: TrainingCalendarDay, n: TrainingCalendarDay) =
            o == n
    }
}

