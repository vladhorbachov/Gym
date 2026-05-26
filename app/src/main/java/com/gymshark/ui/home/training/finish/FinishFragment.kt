package com.gymshark.ui.home.training.finish

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentFinishBinding
import com.gymshark.utils.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FinishFragment : BaseFragment<FragmentFinishBinding>(FragmentFinishBinding::inflate) {

    private val finishVm: FinishViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBad.setOnClickListener { finishVm.selectMood(Mood.BAD) }
        binding.btnNeutral.setOnClickListener { finishVm.selectMood(Mood.NEUTRAL) }
        binding.btnGood.setOnClickListener { finishVm.selectMood(Mood.GOOD) }
        binding.btnAmazing.setOnClickListener { finishVm.selectMood(Mood.AMAZING) }
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.pulseFragment)
        }
    }
}
