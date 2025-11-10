package com.gymshark.utils.view.calendarview.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.models.TrainingCalendarDay
import com.gymshark.data.models.Train
import com.gymshark.databinding.ItemDayBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DaysAdapter(
    private val onClick: (Train?) -> Unit
) : ListAdapter<TrainingCalendarDay, DaysAdapter.DayVH>(DiffCallback()) {

    private val today = Calendar.getInstance()
    var trainingDays: Set<Int> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayVH(binding)
    }

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayVH(private val binding: ItemDayBinding) :
        RecyclerView.ViewHolder(binding.root) {
/**
 * TODO:  передати айді останнььго тренування
 * отримати список trainingSlot
 * написати укстеншн для трейнінг слота де я передаю айді і поточний день а він мені передає наступне тренування
 * написати екстеншн для розрахунку праивльного дня
 * */
        fun bind(item: TrainingCalendarDay) {
            val dateFormat = SimpleDateFormat("d", Locale.getDefault())
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

            binding.dayNumber.text = dateFormat.format(Date(item.date))
            binding.dayName.text = dayFormat.format(Date(item.date))

            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = todayCal.timeInMillis

            val cal = Calendar.getInstance().apply { timeInMillis = item.date }
            val isToday = cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

            val hasTrain = item.train != null
            val isFutureDay = item.date > startOfToday
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

            val isPlannedFutureTrainDay = isFutureDay && trainingDays.contains(dayOfWeek)

            when {
                isToday -> {
                    setBgText("#2196F3", Color.WHITE, Color.WHITE)
                }
                hasTrain -> {
                    setBgText("#4CAF50", Color.WHITE, Color.WHITE)
                }
                isPlannedFutureTrainDay -> {
                    setBgText("#9C27B0", Color.WHITE, Color.WHITE)
                }
                else -> {
                    setBgTextTransparent()
                }
            }

            binding.root.setOnClickListener { onClick(item.train) }
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
        override fun areItemsTheSame(oldItem: TrainingCalendarDay, newItem: TrainingCalendarDay) =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: TrainingCalendarDay, newItem: TrainingCalendarDay) = oldItem == newItem
    }
}