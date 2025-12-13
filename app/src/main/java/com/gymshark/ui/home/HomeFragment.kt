package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val recommendedAdapter by lazy {
        RecommendedExercisesAdapter { }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        with(binding) {
            rvExercises.layoutManager =
                LinearLayoutManager(requireContext())

            rvExercises.adapter = recommendedAdapter

            cvCalendar.onDayClick = { date, train, types ->
                trainingVm.loadSuggestedExercises(types)

            }
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

                        recommendedAdapter.submitList(sectionedItems)
                    }
                }


            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
