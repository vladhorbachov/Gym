package com.gymshark.ui.home.profile.modal

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemTypeBinding

class TrainingTypesAdapter :
    ListAdapter<String, TrainingTypesAdapter.TypeVH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TypeVH(ItemTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TypeVH, position: Int) =
        holder.bind(getItem(position))

    inner class TypeVH(private val b: ItemTypeBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(type: String) {
            b.title.text = type

            b.root.setOnLongClickListener {
                val data = ClipData.newPlainText("type", type)
                val shadow = View.DragShadowBuilder(it)
                it.startDragAndDrop(data, shadow, null, 0)
                true
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a == b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }
}
