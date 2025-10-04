package com.gymshark.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gymshark.databinding.FragmentOnboardingtwoBinding
import com.gymshark.utils.WaveView

class OnboardingFragmentTwo : Fragment() {

    private var _binding: FragmentOnboardingtwoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingtwoBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
