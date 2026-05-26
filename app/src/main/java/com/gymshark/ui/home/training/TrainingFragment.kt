package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingBinding
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class TrainingFragment : BaseFragment<FragmentTrainingBinding>(FragmentTrainingBinding::inflate) {

    private val trainingVm: TrainingViewModel by activityViewModel()
    private val statVm: TrainingStatViewModel by activityViewModel()

    private val adapter = ExerciseAdapter { item ->
        ExerciseActionsBottomSheet.newInstance(item.exerciseId)
            .show(childFragmentManager, "exercise_actions")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            rvExercises.layoutManager = LinearLayoutManager(requireContext())
            rvExercises.adapter = adapter
            rvExercises.addItemDecoration(
                DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            )

            btnFinish.setOnClickListener {
                statVm.stopAndReset()
                findNavController().navigate(R.id.finishFragment)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            trainingVm.suggestedExercisesFlow.collect { list ->
                trainingVm.setExercisesFromSuggested(list)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            trainingVm.draft.collect { draft ->
                adapter.submitList(draft.exercises)
            }
        }
    }
}
