package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentPrepareTrainBinding

class PrepareFragment : Fragment(R.layout.fragment_prepare_train) {
    private var _binding: FragmentPrepareTrainBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        _binding = FragmentPrepareTrainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrepareTrainBinding.bind(view)

        with(binding) {
            btnStartTrain.setOnClickListener {
                findNavController().navigate(R.id.action_prepareFragment_to_navTraining)
            }
        }
    }
    fun startTimer(){

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}