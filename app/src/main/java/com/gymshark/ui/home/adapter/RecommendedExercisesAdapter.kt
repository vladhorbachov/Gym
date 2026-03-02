package com.gymshark.ui.home.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.databinding.ItemExerciseCardBinding
import com.gymshark.databinding.ItemExerciseHeaderBinding

class RecommendedExercisesAdapter(
    private val onExerciseClick: (ExercisesEntity) -> Unit
) : ListAdapter<ExerciseListItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {

        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_EXERCISE = 1
        private const val PAYLOAD_COMPLETED = "payload_completed"

        private val DIFF = object : DiffUtil.ItemCallback<ExerciseListItem>() {

            override fun areItemsTheSame(
                oldItem: ExerciseListItem,
                newItem: ExerciseListItem
            ): Boolean {
                return when {
                    oldItem is ExerciseListItem.Header && newItem is ExerciseListItem.Header ->
                        oldItem.title == newItem.title

                    oldItem is ExerciseListItem.ExerciseRow && newItem is ExerciseListItem.ExerciseRow ->
                        oldItem.exercise.id == newItem.exercise.id

                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: ExerciseListItem,
                newItem: ExerciseListItem
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: ExerciseListItem,
                newItem: ExerciseListItem
            ): Any? = null
        }
    }

    /**
     * Реальная логика "completed": сюда HomeFragment отдаёт set exerciseId из trainsFlow.
     */
    private var completedIds: Set<Int> = emptySet()

    fun setCompletedIds(ids: Set<Int>) {
        if (ids == completedIds) return
        completedIds = ids

        // Обновляем только строки упражнений (без notifyDataSetChanged).
        // Да, это O(n), но n у тебя небольшой, и обновление точечное.
        for (i in 0 until itemCount) {
            if (getItem(i) is ExerciseListItem.ExerciseRow) {
                notifyItemChanged(i, PAYLOAD_COMPLETED)
            }
        }
    }

    /**
     * Нужен для drag start/end из ItemTouchHelper.
     */
    fun getExerciseVH(holder: RecyclerView.ViewHolder): ExerciseVH? = holder as? ExerciseVH

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ExerciseListItem.Header -> VIEW_TYPE_HEADER
            is ExerciseListItem.ExerciseRow -> VIEW_TYPE_EXERCISE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderVH(ItemExerciseHeaderBinding.inflate(inflater, parent, false))
            else -> ExerciseVH(ItemExerciseCardBinding.inflate(inflater, parent, false), onExerciseClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ExerciseListItem.Header -> (holder as HeaderVH).bind(item)
            is ExerciseListItem.ExerciseRow -> (holder as ExerciseVH).bind(item.exercise, isCompleted(item.exercise))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_COMPLETED) && holder is ExerciseVH) {
            val item = getItem(position) as? ExerciseListItem.ExerciseRow ?: return
            holder.bindCompletedOnly(isCompleted(item.exercise))
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    private fun isCompleted(exercise: ExercisesEntity): Boolean = completedIds.contains(exercise.id)

    private class HeaderVH(
        private val binding: ItemExerciseHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExerciseListItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    class ExerciseVH(
        private val binding: ItemExerciseCardBinding,
        private val onExerciseClick: (ExercisesEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var boundExercise: ExercisesEntity? = null

        init {
            // Click
            binding.card.setOnClickListener {
                boundExercise?.let(onExerciseClick)
            }

            // Press animation (scale 0.97)
            applyPressAnimation(binding.card)
        }

        fun bind(exercise: ExercisesEntity, completed: Boolean) = with(binding) {
            boundExercise = exercise

            tvTitle.text = exercise.name
            tvMeta.text = buildMeta(exercise)

            completedBar.isVisible = completed

            // drag icon baseline
            ivDrag.alpha = 0.5f

            // reset transformations (важно при recycle)
            card.scaleX = 1f
            card.scaleY = 1f
            card.cardElevation = 2f
        }

        fun bindCompletedOnly(completed: Boolean) {
            binding.completedBar.isVisible = completed
        }

        private fun buildMeta(exercise: ExercisesEntity): String {
            // Здесь уже "живой план", но без фейка.
            // difficulty — реально из сущности. Sets summary — появится, когда ты начнёшь хранить план/сеты на этот день.
            return "Difficulty ${exercise.difficulty} · ready to train"
        }

        fun onDragStart() {
            binding.card.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(140)
                .start()

            binding.card.cardElevation = 6f
            binding.ivDrag.alpha = 1f
        }

        fun onDragEnd() {
            binding.card.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(160)
                .start()

            binding.card.cardElevation = 2f
            binding.ivDrag.alpha = 0.5f
        }

        private fun applyPressAnimation(view: View) {
            view.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(160).start()
                    }
                }
                false
            }
        }
    }

}