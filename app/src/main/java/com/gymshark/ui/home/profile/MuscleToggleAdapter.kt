package com.gymshark.ui.home.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemMuscleToggleChipBinding

data class MuscleToggleChip(
    val name: String,
    val selected: Boolean
)

class MuscleToggleAdapter(
    private val onTypeClick: (String) -> Unit
) : ListAdapter<MuscleToggleChip, MuscleToggleAdapter.MuscleVH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuscleVH =
        MuscleVH(ItemMuscleToggleChipBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MuscleVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MuscleVH(private val b: ItemMuscleToggleChipBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MuscleToggleChip) = with(b) {
            root.isSelected = item.selected
            tvTitle.text = item.name
            tvState.text = if (item.selected) "Added" else "Add"
            root.setOnClickListener { onTypeClick(item.name) }
        }
    }

    class Diff : DiffUtil.ItemCallback<MuscleToggleChip>() {
        override fun areItemsTheSame(a: MuscleToggleChip, b: MuscleToggleChip): Boolean = a.name == b.name
        override fun areContentsTheSame(a: MuscleToggleChip, b: MuscleToggleChip): Boolean = a == b
    }
}
