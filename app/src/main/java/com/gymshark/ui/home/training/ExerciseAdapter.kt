package com.gymshark.ui.home.training

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemExerciseBinding
import com.gymshark.ui.home.training.drafts.ExerciseDraft
import com.gymshark.ui.home.training.drafts.SetEntry

class ExerciseAdapter(
    private val onClick: (ExerciseDraft) -> Unit
) : ListAdapter<ExerciseDraft, ExerciseAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ExerciseDraft>() {
            override fun areItemsTheSame(old: ExerciseDraft, new: ExerciseDraft): Boolean =
                old.exerciseId == new.exerciseId

            override fun areContentsTheSame(old: ExerciseDraft, new: ExerciseDraft): Boolean =
                old == new
        }
    }

    inner class VH(private val b: ItemExerciseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ExerciseDraft) = with(b) {
            tvTitle.text = item.title
            tvSetsReps.text = formatSets(item.sets)
            tvWeight.text = formatMaxWeight(item.sets)
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(ItemExerciseBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    private fun formatSets(sets: List<SetEntry>): String {
        if (sets.isEmpty()) return "—"
        return sets.joinToString(", ") { s ->
            val reps = s.reps?.toString() ?: "—"
            val w = s.weight?.let { weight ->
                if (weight % 1f == 0f) weight.toInt().toString() else "%.1f".format(weight)
            } ?: "—"
            "${reps}×${w}"
        }
    }

    private fun formatMaxWeight(sets: List<SetEntry>): String {
        val max = sets.mapNotNull { it.weight }.maxOrNull() ?: return "—"
        val s = if (max % 1f == 0f) max.toInt().toString() else "%.1f".format(max)
        return "$s kg"
    }
}
