package com.gymshark.ui.home.profile.modal

import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.databinding.ItemSlotBinding
import com.gymshark.domain.models.DaySlot
import java.time.format.TextStyle
import java.util.Locale

class TrainingSlotsAdapter(
    private val onTypeDropped: (slot: DaySlot, type: String) -> Unit,
    private val onClearSlot: (slot: DaySlot) -> Unit,
    private val onRemoveType: (slot: DaySlot, type: String) -> Unit
) : ListAdapter<DaySlot, TrainingSlotsAdapter.SlotVH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotVH =
        SlotVH(ItemSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: SlotVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SlotVH(private val b: ItemSlotBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(slot: DaySlot) = with(b) {
            val assignedTypes = slot.types.filter { it.isNotBlank() }.distinct()
            day.text = slot.day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
            renderTypeChips(slot, assignedTypes)
            btnClear.visibility = if (assignedTypes.isEmpty()) View.INVISIBLE else View.VISIBLE
            btnClear.setOnClickListener { onClearSlot(slot) }
            setSurface(slot, active = false)

            root.setOnDragListener { _, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        setSurface(slot, active = true)
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED,
                    DragEvent.ACTION_DRAG_ENDED -> {
                        setSurface(slot, active = false)
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        val type = event.clipData?.getItemAt(0)?.text?.toString().orEmpty()
                        setSurface(slot, active = false)
                        if (type.isNotBlank()) onTypeDropped(slot, type)
                        true
                    }
                    else -> true
                }
            }
        }

        private fun renderTypeChips(slot: DaySlot, assignedTypes: List<String>) = with(b) {
            typesContainer.removeAllViews()

            if (assignedTypes.isEmpty()) {
                typesContainer.addView(
                    TextView(root.context).apply {
                        text = "Drop muscle"
                        setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                        textSize = 14f
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        gravity = Gravity.CENTER_VERTICAL
                    },
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                return
            }

            assignedTypes.forEach { type ->
                typesContainer.addView(
                    TextView(root.context).apply {
                        text = "$type  x"
                        setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
                        textSize = 12f
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        gravity = Gravity.CENTER
                        background = ContextCompat.getDrawable(context, R.drawable.bg_slot_muscle_chip)
                        setPadding(12.dp(), 0, 12.dp(), 0)
                        setOnClickListener { onRemoveType(slot, type) }
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        34.dp()
                    ).apply {
                        marginEnd = 8.dp()
                    }
                )
            }
        }

        private fun setSurface(slot: DaySlot, active: Boolean) = with(b) {
            val assigned = slot.types.any { it.isNotBlank() }
            slotSurface.background = ContextCompat.getDrawable(
                root.context,
                when {
                    active -> R.drawable.bg_slot_drag_active
                    assigned -> R.drawable.bg_slot_assigned
                    else -> R.drawable.bg_slot_default
                }
            )
            root.animate()
                .scaleX(if (active) 1.04f else 1f)
                .scaleY(if (active) 1.04f else 1f)
                .setDuration(120L)
                .start()
        }
    }

    class Diff : DiffUtil.ItemCallback<DaySlot>() {
        override fun areItemsTheSame(a: DaySlot, b: DaySlot): Boolean = a.id == b.id
        override fun areContentsTheSame(a: DaySlot, b: DaySlot): Boolean = a == b
    }

    private fun Int.dp(): Int = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
}
