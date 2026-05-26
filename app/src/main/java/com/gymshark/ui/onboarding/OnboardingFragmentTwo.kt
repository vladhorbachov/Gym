package com.gymshark.ui.onboarding

import android.os.Bundle
import android.view.View
import com.gymshark.databinding.FragmentOnboardingtwoBinding
import com.gymshark.utils.BaseFragment
import com.gymshark.utils.view.WaveView

class OnboardingFragmentTwo :
    BaseFragment<FragmentOnboardingtwoBinding>(FragmentOnboardingtwoBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.wave.setHarmonics(
            listOf(
                WaveView.Harmonic(amplitude = 10f, frequency = 5f, phase = 2f),
                WaveView.Harmonic(amplitude = 20f, frequency = 3f, phase = 1f),
                WaveView.Harmonic(amplitude = 50f, frequency = 1f, phase = 0f),
                WaveView.Harmonic(amplitude = 20f, frequency = 3f, phase = 1f),
                WaveView.Harmonic(amplitude = 10f, frequency = 5f, phase = 2f),
                WaveView.Harmonic(amplitude = 20f, frequency = 3f, phase = 1f),
                WaveView.Harmonic(amplitude = 50f, frequency = 1f, phase = 0f),
                WaveView.Harmonic(amplitude = 20f, frequency = 3f, phase = 1f),
                WaveView.Harmonic(amplitude = 50f, frequency = 1f, phase = 0f),
                WaveView.Harmonic(amplitude = 20f, frequency = 3f, phase = 1f),
                WaveView.Harmonic(amplitude = 10f, frequency = 5f, phase = 2f)
            )
        )
    }
}
