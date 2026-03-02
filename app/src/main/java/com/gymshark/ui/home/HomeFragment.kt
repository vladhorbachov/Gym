package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.databinding.FragmentHomeBinding
import com.gymshark.ui.home.adapter.ExerciseListItem
import com.gymshark.ui.home.adapter.RecommendedExercisesAdapter
import com.gymshark.ui.home.training.TrainingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val trainingVm: TrainingViewModel by activityViewModel()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var lastSelectedTypes: List<String> = emptyList()
    private val recommendedAdapter by lazy {
        RecommendedExercisesAdapter { exercise ->
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = recommendedAdapter

        attachTouchHelper()
        binding.todayCard.root.setOnClickListener {

            if (trainingVm.draft.value.exercises.isEmpty()) return@setOnClickListener

            findNavController().navigate(R.id.action_navHome_to_train_graph)
        }


        binding.cvCalendar.onDayClick = label@{ _, _, types ->
            if (types == lastSelectedTypes) return@label
            lastSelectedTypes = types
            trainingVm.loadSuggestedExercises(types)
        }



        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    trainingVm.suggestedExercisesFlow.collect { list ->
                        trainingVm.setExercisesFromSuggested(list)
                    }
                }
                launch {
                    trainingVm.trainingDaysFlow.collect { calendarDays ->
                        binding.cvCalendar.setTrainingDays(calendarDays)
                    }
                }

                launch {
                    trainingVm.trainingSlotsFlow.collect { slots ->
                        binding.cvCalendar.setTrainingSlots(slots)
                    }
                }

                launch {
                    trainingVm.draft.collect { draft ->

                        val sectionedItems =
                            draft.exercises
                                .groupBy { it.baseCategory }
                                .flatMap { (category, list) ->
                                    listOf(ExerciseListItem.Header(category)) +
                                            list.map { draftExercise ->
                                                ExerciseListItem.ExerciseRow(
                                                    com.gymshark.data.db.entity.ExercisesEntity(
                                                        id = draftExercise.exerciseId.toInt(),
                                                        name = draftExercise.title,
                                                        baseCategory = draftExercise.baseCategory,
                                                        subCategory = "",
                                                        difficulty = 1
                                                    )
                                                )
                                            }
                                }
                        updateTodayCard(draft.exercises.size)
                        binding.rvExercises.isVisible = draft.exercises.isNotEmpty()

                        recommendedAdapter.submitList(sectionedItems)
                    }
                }

                launch {
                    trainingVm.trainsFlow.collect { trains ->
                        binding.cvCalendar.setTrains(trains)
                    }
                }
                launch {
                    trainingVm.completedExerciseIdsFlow.collect { ids ->
                        recommendedAdapter.setCompletedIds(ids)
                    }
                }
            }
        }

    }

    private fun updateTodayCard(exCount: Int) {
        binding.todayCard.root.alpha = if (exCount > 0) 1f else 0.5f
        binding.todayCard.root.isClickable = exCount > 0

        val title = if (lastSelectedTypes.isEmpty())
            "Rest day"
        else
            lastSelectedTypes.joinToString(", ")
                .replaceFirstChar { it.uppercase() }

        binding.todayCard.tvTrainingName.text = title

        binding.todayCard.tvMeta.text =
            if (exCount == 0) "No exercises"
            else "$exCount exercises"
    }


    private fun attachTouchHelper() {
        val callback = object : androidx.recyclerview.widget.ItemTouchHelper.Callback() {

            override fun isLongPressDragEnabled() = true
            override fun isItemViewSwipeEnabled() = true

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return 0

                return when (recommendedAdapter.getItemViewType(pos)) {
                    0 -> makeMovementFlags(0, 0)
                    else -> makeMovementFlags(
                        androidx.recyclerview.widget.ItemTouchHelper.UP or
                                androidx.recyclerview.widget.ItemTouchHelper.DOWN,
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT
                    )
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                if (fromPos == RecyclerView.NO_POSITION ||
                    toPos == RecyclerView.NO_POSITION) return false

                if (recommendedAdapter.getItemViewType(toPos) == 0) return false

                val fromExerciseIndex = getExerciseIndex(fromPos)
                val toExerciseIndex = getExerciseIndex(toPos)

                if (fromExerciseIndex == -1 || toExerciseIndex == -1) return false

                trainingVm.reorderExercises(fromExerciseIndex, toExerciseIndex)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return

                if (recommendedAdapter.getItemViewType(pos) == 0) return

                val exerciseIndex = getExerciseIndex(pos)
                if (exerciseIndex == -1) return

                trainingVm.removeExerciseAt(exerciseIndex)
            }

            private fun getExerciseIndex(adapterPosition: Int): Int {

                val currentList = recommendedAdapter.currentList

                var exerciseIndex = -1
                var counter = -1

                currentList.forEachIndexed { index, item ->
                    if (item is ExerciseListItem.ExerciseRow) {
                        counter++
                    }
                    if (index == adapterPosition) {
                        exerciseIndex = counter
                    }
                }

                return exerciseIndex
            }


        }

        androidx.recyclerview.widget.ItemTouchHelper(callback).attachToRecyclerView(binding.rvExercises)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
