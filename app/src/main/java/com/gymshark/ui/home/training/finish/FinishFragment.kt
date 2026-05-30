package com.gymshark.ui.home.training.finish

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentFinishBinding
import com.gymshark.utils.BaseFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FinishFragment : BaseFragment<FragmentFinishBinding>(FragmentFinishBinding::inflate) {

    private val finishVm: FinishViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playIntro()

        binding.btnBack.setOnClickListener {
            it.pressPulse()
            findNavController().navigateUp()
        }

        binding.btnBad.setOnClickListener { selectMood(Mood.BAD, it) }
        binding.btnNeutral.setOnClickListener { selectMood(Mood.NEUTRAL, it) }
        binding.btnGood.setOnClickListener { selectMood(Mood.GOOD, it) }
        binding.btnAmazing.setOnClickListener { selectMood(Mood.AMAZING, it) }
        binding.btnContinue.setOnClickListener {
            it.pressPulse()
            findNavController().navigate(R.id.pulseFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                finishVm.mood.collect { mood ->
                    renderMood(mood)
                }
            }
        }
    }

    private fun selectMood(mood: Mood, view: View) {
        view.pressPulse()
        finishVm.selectMood(mood)
    }

    private fun renderMood(selected: Mood) = with(binding) {
        val buttons = mapOf(
            Mood.BAD to btnBad,
            Mood.NEUTRAL to btnNeutral,
            Mood.GOOD to btnGood,
            Mood.AMAZING to btnAmazing
        )
        buttons.forEach { (mood, button) ->
            button.applyMoodState(mood == selected)
        }
        tvSelectedMood.text = "Mood: ${selected.label}"
    }

    private val Mood.label: String
        get() = when (this) {
            Mood.BAD -> "Hard"
            Mood.NEUTRAL -> "Steady"
            Mood.GOOD -> "Strong"
            Mood.AMAZING -> "Peak"
        }

    private fun MaterialButton.applyMoodState(selected: Boolean) {
        val context = requireContext()
        val surface = ContextCompat.getColor(context, R.color.trainingSurfaceRaised)
        val selectedColor = ContextCompat.getColor(context, R.color.trainingAction)
        val stroke = ContextCompat.getColor(context, R.color.trainingOutline)
        val actionText = ContextCompat.getColor(context, R.color.trainingAction)
        val selectedText = ContextCompat.getColor(context, R.color.trainingOnAction)

        backgroundTintList = ColorStateList.valueOf(if (selected) selectedColor else surface)
        strokeColor = ColorStateList.valueOf(if (selected) selectedColor else stroke)
        setTextColor(if (selected) selectedText else actionText)
        animate()
            .scaleX(if (selected) 1.03f else 1f)
            .scaleY(if (selected) 1.03f else 1f)
            .setDuration(160L)
            .start()
    }

    private fun playIntro() = with(binding) {
        listOf(btnBack, tvKicker, tvTitle, tvSubtitle, moodGrid, cardNext, btnContinue)
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
