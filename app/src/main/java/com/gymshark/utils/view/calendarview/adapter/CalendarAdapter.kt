package com.gymshark.utils.view.calendarview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.models.ItemDays
import com.gymshark.databinding.ItemCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarAdapter() : ListAdapter<ItemDays, CalendarAdapter.VH>(object:DiffUtil.ItemCallback<ItemDays>(){
    override fun areItemsTheSame(
        oldItem: ItemDays,
        newItem: ItemDays
    ): Boolean {
        return oldItem.data == newItem.data && oldItem.type == newItem.type
    }

    override fun areContentsTheSame(
        oldItem: ItemDays,
        newItem: ItemDays
    ): Boolean {
        return oldItem == newItem
    }

}) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return VH(ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemCalendarBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ItemDays){
            val calendar = Calendar.getInstance().apply {
                timeInMillis = item.data
            }

            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)

            val dayName = SimpleDateFormat("EEEE", Locale("uk")).format(calendar.time)
            with(binding) {
                tvDays.text = dayName
                tvNum.text = "$dayNumber"
            }


        }
    }
}