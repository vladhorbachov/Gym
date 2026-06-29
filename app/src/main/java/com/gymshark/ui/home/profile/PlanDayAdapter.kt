package com.gymshark.ui.home.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemPlanDayChipBinding
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

data class PlanDayChip(
    val day: DayOfWeek,
    val selected: Boolean,
    val assignedCount: Int
)

class PlanDayAdapter(
    private val onDayClick: (DayOfWeek) -> Unit
) : ListAdapter<PlanDayChip, PlanDayAdapter.DayVH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH =
        DayVH(ItemPlanDayChipBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: DayVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayVH(private val b: ItemPlanDayChipBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: PlanDayChip) = with(b) {
            val locale = Locale.getDefault()
            tvDayName.text = item.day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale)
            tvCount.text = when (item.assignedCount) {
                0 -> "No focus"
                1 -> "1 focus"
                else -> "${item.assignedCount} focuses"
            }
            root.isSelected = item.selected
            root.setOnClickListener { onDayClick(item.day) }
        }
    }

    class Diff : DiffUtil.ItemCallback<PlanDayChip>() {
        override fun areItemsTheSame(a: PlanDayChip, b: PlanDayChip): Boolean = a.day == b.day
        override fun areContentsTheSame(a: PlanDayChip, b: PlanDayChip): Boolean = a == b
    }
}
