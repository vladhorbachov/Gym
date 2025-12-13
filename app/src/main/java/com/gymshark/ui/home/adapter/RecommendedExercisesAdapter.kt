package com.gymshark.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.databinding.ItemExerciseHeaderBinding
import com.gymshark.databinding.ItemRecommendedExerciseBinding

class RecommendedExercisesAdapter(
    private val onExerciseClick: (ExercisesEntity) -> Unit
) : ListAdapter<ExerciseListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_EXERCISE = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ExerciseListItem.Header -> VIEW_TYPE_HEADER
            is ExerciseListItem.ExerciseRow -> VIEW_TYPE_EXERCISE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemExerciseHeaderBinding.inflate(inflater, parent, false)
                HeaderVH(binding)
            }
            else -> {
                val binding = ItemRecommendedExerciseBinding.inflate(inflater, parent, false)
                ExerciseVH(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ExerciseListItem.Header -> (holder as HeaderVH).bind(item)
            is ExerciseListItem.ExerciseRow -> (holder as ExerciseVH).bind(item.exercise)
        }
    }

    inner class HeaderVH(
        private val binding: ItemExerciseHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExerciseListItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    inner class ExerciseVH(
        private val binding: ItemRecommendedExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ExercisesEntity) {
            binding.tvName.text = exercise.name

            binding.tvSubtitle.text = "Level ${exercise.difficulty}"

            binding.root.setOnClickListener {
                onExerciseClick(exercise)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExerciseListItem>() {
        override fun areItemsTheSame(
            oldItem: ExerciseListItem,
            newItem: ExerciseListItem
        ): Boolean = when {
            oldItem is ExerciseListItem.Header && newItem is ExerciseListItem.Header ->
                oldItem.title == newItem.title

            oldItem is ExerciseListItem.ExerciseRow && newItem is ExerciseListItem.ExerciseRow ->
                oldItem.exercise.id == newItem.exercise.id

            else -> false
        }

        override fun areContentsTheSame(
            oldItem: ExerciseListItem,
            newItem: ExerciseListItem
        ): Boolean = oldItem == newItem
    }
}
