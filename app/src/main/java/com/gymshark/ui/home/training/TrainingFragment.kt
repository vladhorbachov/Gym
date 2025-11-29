package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.data.models.Exercise
import com.gymshark.databinding.FragmentTrainingBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrainingFragment : Fragment(R.layout.fragment_training),
    ExerciseActionsBottomSheet.Callbacks {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!
    private val trainingVm: TrainingViewModel by viewModel()

    private val adapter = ExerciseAdapter { item ->
        ExerciseActionsBottomSheet.newInstance(item)
            .show(childFragmentManager, "exercise_actions")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            rvExercises.layoutManager = LinearLayoutManager(requireContext())
            rvExercises.adapter = adapter
            rvExercises.addItemDecoration(
                DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            )

            val testData = listOf(
                Exercise(1, "Bench Press", "—", "—"),
                Exercise(2, "Squat", "—", "—"),
                Exercise(3, "Deadlift", "—", "—")
            )
            adapter.submitList(testData)


        }
    }

    override fun onInfoClicked(exercise: Exercise) {
        // TODO: реалізація
    }

    private fun formatSetsReps(avgReps: Float?, sets: Int): String? {
        avgReps?.let {
            val whole = it.toInt()
            val repsStr = if (it == whole.toFloat()) whole.toString() else "%.1f".format(it)
            return "$repsStr x $sets"
        }
        return "— x $sets"
    }

    override fun onExerciseParamsChanged(
        exerciseId: Long,
        maxWeight: Float?,
        avgReps: Float?,
        sets: Int
    ) {
        val newList = adapter.currentList.map { ex ->
            if (ex.id == exerciseId) {
                ex.copy(
                    weight = maxWeight?.let { formatWeight(it) } ?: ex.weight,
                    setsReps = formatSetsReps(avgReps, sets) ?: ex.setsReps
                )
            } else ex
        }
        adapter.submitList(newList)
    }

    private fun formatWeight(value: Float): String {
        val asInt = value.toInt()
        return if (value == asInt.toFloat()) "$asInt kg" else "%.1f kg".format(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
