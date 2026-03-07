package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/*
* Розширити табличку щоб сетсрепс збергіав і вагу і кількість повторів
* Створити табличку з максимальною вагою, айді вправи, назва вправи, загальна кількість підходів за все життя на кожну вправу, також зберігати айдішку тренування в якому було досягнуто максимальну вагу
* для показу в статс фрагмент
* фінальний фрагмент після тренування в якому я можу вибрати настрій,та перехід до вимірювання пусльсу по ікноці підчас тренування та в кінці
* додати кнопку для переходу в вимір пульсу по іконці анімованій
*
* */
class TrainingFragment : Fragment(R.layout.fragment_training) {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!

    private val trainingVm: TrainingViewModel by activityViewModel()
    private val statVm: TrainingStatViewModel by activityViewModel()

    private val adapter = ExerciseAdapter { item ->
        ExerciseActionsBottomSheet.newInstance(item.exerciseId)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
