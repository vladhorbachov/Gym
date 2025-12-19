package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private var currentItems: MutableList<ExerciseListItem> = mutableListOf()

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

        binding.cvCalendar.onDayClick = { _, _, types ->
            trainingVm.loadSuggestedExercises(types)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    trainingVm.trainsFlow.collect { trains ->
                        binding.cvCalendar.setTrains(trains)
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
                    trainingVm.suggestedExercisesFlow.collect { exercises ->
                        val sectionedItems: List<ExerciseListItem> =
                            exercises
                                .groupBy { it.baseCategory }
                                .flatMap { (category, list) ->
                                    listOf(ExerciseListItem.Header(category)) +
                                            list.map { ExerciseListItem.ExerciseRow(it) }
                                }

                        currentItems = sectionedItems.toMutableList()
                        recommendedAdapter.submitList(currentItems.toList())
                    }
                }
            }
        }

    }

    private fun attachTouchHelper() {
        val callback = object : androidx.recyclerview.widget.ItemTouchHelper.Callback() {

            override fun isLongPressDragEnabled() = true
            override fun isItemViewSwipeEnabled() = true

            override fun getMovementFlags(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Int {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return 0

                return when (recommendedAdapter.getItemViewType(pos)) {
                    0 -> makeMovementFlags(0, 0) // Header: нічого
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
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false

                if (recommendedAdapter.getItemViewType(to) == 0) return false

                val fromHeader = headerIndexAbove(from)
                val toHeader = headerIndexAbove(to)
                if (fromHeader != toHeader) return false

                java.util.Collections.swap(currentItems, from, to)
                recommendedAdapter.submitList(currentItems.toList())
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return

                if (recommendedAdapter.getItemViewType(pos) == 0) {
                    recommendedAdapter.submitList(currentItems.toList())
                    return
                }

                currentItems.removeAt(pos)

                removeLonelyHeaders()

                recommendedAdapter.submitList(currentItems.toList())
            }

            private fun headerIndexAbove(position: Int): Int {
                for (i in position downTo 0) {
                    if (currentItems[i] is ExerciseListItem.Header) return i
                }
                return -1
            }

            private fun removeLonelyHeaders() {
                val toRemove = mutableListOf<Int>()
                var i = 0
                while (i < currentItems.size) {
                    if (currentItems[i] is ExerciseListItem.Header) {
                        val headerIdx = i
                        var hasRows = false
                        var j = i + 1
                        while (j < currentItems.size && currentItems[j] !is ExerciseListItem.Header) {
                            if (currentItems[j] is ExerciseListItem.ExerciseRow) hasRows = true
                            j++
                        }
                        if (!hasRows) toRemove.add(headerIdx)
                        i = j
                    } else i++
                }
                toRemove.asReversed().forEach { currentItems.removeAt(it) }
            }
        }

        androidx.recyclerview.widget.ItemTouchHelper(callback).attachToRecyclerView(binding.rvExercises)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
