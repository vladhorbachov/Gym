package com.gymshark.ui.home.training

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.models.Exercise
import com.gymshark.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val onClick: (Exercise) -> Unit
) : ListAdapter<Exercise, ExerciseAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Exercise>() {
            override fun areItemsTheSame(old: Exercise, new: Exercise) = old.id == new.id
            override fun areContentsTheSame(old: Exercise, new: Exercise) = old == new
        }
    }

    inner class VH(private val b: ItemExerciseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Exercise) = with(b) {
            tvTitle.text = item.title
            tvSetsReps.text = item.setsReps
            tvWeight.text = item.weight
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(ItemExerciseBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}
