package com.gymshark.ui.home.training.finish

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.gymshark.R

class FinishFragment : Fragment(R.layout.fragment_finish) {

    private val finishVm: FinishViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBad)
            .setOnClickListener { finishVm.selectMood(Mood.BAD) }

        view.findViewById<View>(R.id.btnNeutral)
            .setOnClickListener { finishVm.selectMood(Mood.NEUTRAL) }

        view.findViewById<View>(R.id.btnGood)
            .setOnClickListener { finishVm.selectMood(Mood.GOOD) }

        view.findViewById<View>(R.id.btnAmazing)
            .setOnClickListener { finishVm.selectMood(Mood.AMAZING) }

        view.findViewById<View>(R.id.btnContinue).setOnClickListener {
            findNavController().navigate(R.id.pulseFragment)
        }
    }
}
