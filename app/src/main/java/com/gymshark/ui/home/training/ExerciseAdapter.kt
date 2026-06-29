package com.gymshark.ui.home.training

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.databinding.ItemExerciseBinding
import com.gymshark.ui.home.training.drafts.ExerciseDraft
import com.gymshark.ui.home.training.drafts.SetEntry
import com.gymshark.ui.home.training.drafts.isPerformed

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
        fun bind(item: ExerciseDraft, position: Int) = with(b) {
            tvIndex.text = (position + 1).toString()
            tvTitle.text = item.title
            tvSetsReps.text = formatSets(item.sets)
            tvWeight.text = formatMaxWeight(item.sets)
            tvSetSummary.text = formatSetSummary(item.sets)

            root.setOnClickListener { onClick(item) }
            btnNext.setOnClickListener { onClick(item) }

            root.alpha = 0f
            root.scaleX = 0.98f
            root.scaleY = 0.98f
            root.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay((position.coerceAtMost(6) * 35).toLong())
                .setDuration(180L)
                .start()
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(ItemExerciseBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos), pos)

    private fun formatSets(sets: List<SetEntry>): String {
        if (sets.isEmpty()) return "No sets yet"
        return sets.joinToString(", ") { set ->
            val reps = set.reps?.toString() ?: "-"
            val weight = set.weight?.let { value ->
                if (value % 1f == 0f) value.toInt().toString() else "%.1f".format(value)
            } ?: "-"
            "$reps x $weight"
        }
    }

    private fun formatMaxWeight(sets: List<SetEntry>): String {
        val max = sets.mapNotNull { it.weight }.maxOrNull() ?: return "No weight"
        val value = if (max % 1f == 0f) max.toInt().toString() else "%.1f".format(max)
        return "$value kg"
    }

    private fun formatSetSummary(sets: List<SetEntry>): String {
        val done = sets.count { it.isPerformed() }
        return "$done/${sets.size} done"
    }
}
