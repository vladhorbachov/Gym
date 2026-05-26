package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentPrepareTrainBinding
import com.gymshark.utils.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PrepareFragment : BaseFragment<FragmentPrepareTrainBinding>(FragmentPrepareTrainBinding::inflate) {
    private val statVm: TrainingStatViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartTrain.setOnClickListener {
            statVm.start()
            findNavController().navigate(R.id.trainingFragment)
        }
    }
}
