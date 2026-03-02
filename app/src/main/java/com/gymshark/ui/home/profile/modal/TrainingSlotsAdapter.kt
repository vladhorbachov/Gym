package com.gymshark.ui.home.profile.modal

import android.view.DragEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemSlotBinding
import com.gymshark.domain.models.DaySlot

class TrainingSlotsAdapter(
    private val onTypeDropped: (slot: DaySlot, type: String) -> Unit
) : ListAdapter<DaySlot, TrainingSlotsAdapter.SlotVH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SlotVH(ItemSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SlotVH, position: Int) =
        holder.bind(getItem(position))

    inner class SlotVH(private val b: ItemSlotBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(slot: DaySlot) {
            b.day.text = slot.day.name.take(3)
            b.types.text = slot.types.joinToString(", ").ifEmpty { "Drop here" }

            b.root.setOnDragListener { _, event ->
                if (event.action == DragEvent.ACTION_DROP) {
                    val type = event.clipData.getItemAt(0).text.toString()
                    onTypeDropped(slot, type)
                }
                true
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<DaySlot>() {
        override fun areItemsTheSame(a: DaySlot, b: DaySlot) = a.id == b.id
        override fun areContentsTheSame(a: DaySlot, b: DaySlot) = a == b
    }
}
