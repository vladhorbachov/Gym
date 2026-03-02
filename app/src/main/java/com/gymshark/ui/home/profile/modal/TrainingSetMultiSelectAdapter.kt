package com.gymshark.ui.home.profile.modal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemTrainingSetBinding

class TrainingSetMultiSelectAdapter(
    private val items: List<String>,
    private val selected: MutableSet<String>,
    private val onChange: () -> Unit
) : RecyclerView.Adapter<TrainingSetMultiSelectAdapter.VH>() {

    inner class VH(private val b: ItemTrainingSetBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: String) {
            b.tvTitle.text = item
            b.checkbox.isChecked = item in selected

            b.root.setOnClickListener { toggle(item) }
            b.checkbox.setOnClickListener { toggle(item) }
        }

        private fun toggle(item: String) {
            if (selected.contains(item)) selected.remove(item)
            else selected.add(item)

            notifyItemChanged(bindingAdapterPosition)
            onChange()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTrainingSetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
