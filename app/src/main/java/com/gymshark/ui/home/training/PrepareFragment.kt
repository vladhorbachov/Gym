package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentPrepareTrainBinding
import com.gymshark.ui.home.training.drafts.isPerformed
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PrepareFragment : BaseFragment<FragmentPrepareTrainBinding>(FragmentPrepareTrainBinding::inflate) {
    private val statVm: TrainingStatViewModel by activityViewModel()
    private val trainingVm: TrainingViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSteps()
        playIntro()

        binding.btnBack.setOnClickListener {
            it.pressPulse()
            findNavController().navigateUp()
        }

        binding.btnStartTrain.setOnClickListener {
            it.pressPulse()
            statVm.start()
            findNavController().navigate(R.id.trainingFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trainingVm.draft.collect { draft ->
                    binding.tvExerciseCount.text = draft.exercises.size.toString()
                    binding.tvSetCount.text = draft.exercises.sumOf { it.sets.size }.toString()
                    val hasPerformedSets = draft.exercises.any { ex -> ex.sets.any { it.isPerformed() } }
                    binding.tvReadiness.text = if (hasPerformedSets) "4" else "3"
                }
            }
        }
    }

    private fun setupSteps() = with(binding) {
        stepPlan.tvStepNumber.text = "1"
        stepPlan.tvStepTitle.text = "Plan"
        stepPlan.tvStepSubtitle.text = "Suggested exercises are already loaded from today."

        stepWarmup.tvStepNumber.text = "2"
        stepWarmup.tvStepTitle.text = "Warm up"
        stepWarmup.tvStepSubtitle.text = "Prepare joints and first working weights."

        stepExecute.tvStepNumber.text = "3"
        stepExecute.tvStepTitle.text = "Execute"
        stepExecute.tvStepSubtitle.text = "Track sets, weights and reps without leaving the flow."
    }

    private fun playIntro() = with(binding) {
        listOf(btnBack, tvKicker, tvTitle, tvSubtitle, cardOverview, stepper, btnStartTrain)
            .forEachIndexed { index, target ->
                target.alpha = 0f
                target.translationY = 24f
                target.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 55L)
                    .setDuration(260L)
                    .start()
            }
    }

    private fun View.pressPulse() {
        animate().scaleX(0.97f).scaleY(0.97f).setDuration(80L).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
        }.start()
    }
}
