package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingBinding
import com.gymshark.ui.home.training.drafts.TrainingDraft
import com.gymshark.ui.home.training.drafts.isPerformed
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class TrainingFragment : BaseFragment<FragmentTrainingBinding>(FragmentTrainingBinding::inflate) {

    private val trainingVm: TrainingViewModel by activityViewModel()
    private val statVm: TrainingStatViewModel by activityViewModel()
    private var isPaused = false

    private val adapter = ExerciseAdapter { item ->
        ExerciseActionsBottomSheet.newInstance(item.exerciseId)
            .show(childFragmentManager, "exercise_actions")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            applyActionBarInsets()
            rvExercises.layoutManager = LinearLayoutManager(requireContext())
            rvExercises.adapter = adapter

            btnBack.setOnClickListener {
                it.pressPulse()
                findNavController().navigateUp()
            }

            btnPause.setOnClickListener {
                it.pressPulse()
                togglePause()
            }

            btnFinish.setOnClickListener {
                it.pressPulse()
                statVm.stop()
                findNavController().navigate(R.id.finishFragment)
            }
        }

        playIntro()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    statVm.elapsedSeconds.collect { seconds ->
                        binding.tvTimer.text = seconds.formatDuration()
                    }
                }
                launch {
                    trainingVm.suggestedExercisesFlow.collect { list ->
                        trainingVm.setExercisesFromSuggested(list)
                    }
                }
                launch {
                    trainingVm.draft.collect { draft ->
                        adapter.submitList(draft.exercises)
                        renderProgress(draft)
                    }
                }
            }
        }
    }

    private fun renderProgress(draft: TrainingDraft) {
        val totalSets = draft.exercises.sumOf { it.sets.size }
        val completedSets = draft.exercises.sumOf { ex -> ex.sets.count { it.isPerformed() } }
        val progress = if (totalSets == 0) 0 else completedSets * 100 / totalSets

        binding.tvProgressMeta.text = "$completedSets/$totalSets sets"
        binding.progressSets.animateProgressTo(progress)
        binding.tvProgressTitle.text = when {
            totalSets == 0 -> "Session progress"
            completedSets == totalSets -> "All planned sets done"
            completedSets > 0 -> "Keep the rhythm"
            else -> "Session progress"
        }
    }

    private fun applyActionBarInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            workoutActionBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = nav.bottom + 96.dp
            }

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }

    private fun playIntro() = with(binding) {
        listOf(header, rvExercises, workoutActionBar).forEachIndexed { index, target ->
            target.alpha = 0f
            target.translationY = 22f
            target.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 70L)
                .setDuration(260L)
                .start()
        }
    }

    private fun android.widget.ProgressBar.animateProgressTo(value: Int) {
        animate().setDuration(120L).withEndAction {
            progress = value.coerceIn(0, max)
        }.start()
    }

    private fun Long.formatDuration(): String {
        val minutes = this / 60
        val seconds = this % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun togglePause() = with(binding) {
        isPaused = !isPaused
        if (isPaused) {
            statVm.pause()
            tvTrainingStatus.text = "Workout paused"
            btnPause.text = "Resume"
        } else {
            statVm.start()
            tvTrainingStatus.text = "Live workout"
            btnPause.text = "Pause"
        }
    }

    private fun View.pressPulse() {
        animate().scaleX(0.97f).scaleY(0.97f).setDuration(80L).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
        }.start()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
