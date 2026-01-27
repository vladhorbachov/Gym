package com.gymshark.ui.home.pulse

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentPulseBinding
import com.gymshark.ui.home.training.TrainingStatViewModel
import com.gymshark.ui.home.training.TrainingViewModel
import com.gymshark.ui.home.training.finish.FinishViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PulseFragment : Fragment(R.layout.fragment_pulse) {

    private var _binding: FragmentPulseBinding? = null
    private val binding get() = _binding!!

    private val trainingVm: TrainingViewModel by activityViewModel()
    private val finishVm: FinishViewModel by activityViewModel()
    private val pulseVm: PulseViewModel by activityViewModel()
    private val statVm: TrainingStatViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPulseBinding.bind(view)

        binding.btnStart.setOnClickListener {
            pulseVm.startMockMeasurement()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            pulseVm.avgBpm.collect { avg ->
                if (avg != null) {
                    binding.tvResult.text =
                        "Min: ${pulseVm.minBpm.value}  " +
                                "Max: ${pulseVm.maxBpm.value}  " +
                                "Avg: $avg"
                }
            }
        }

        binding.btnFinish.setOnClickListener {
            saveAndExit()
        }
    }

    private fun saveAndExit() {
        val finishTime = System.currentTimeMillis()
        val duration = statVm.elapsedSeconds.value
        val mood = finishVm.mood.value.name.lowercase()

        trainingVm.finishTraining(
            title = "Workout",
            finishTime = finishTime,
            durationSec = duration,
            mood = mood,
            minBpm = pulseVm.minBpm.value,
            maxBpm = pulseVm.maxBpm.value,
            avgBpm = pulseVm.avgBpm.value
        )

        findNavController().navigate(
            R.id.action_pulseFragment_to_navStats
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

