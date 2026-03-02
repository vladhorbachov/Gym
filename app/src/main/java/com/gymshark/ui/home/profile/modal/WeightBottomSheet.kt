package com.gymshark.ui.home.profile.modal

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.gymshark.R
import com.gymshark.databinding.ModalWeightBinding
import com.gymshark.utils.view.hapticConfirm

class WeightBottomSheet(
    private val initialWeight: Float,
    private val listener: WeightResultListener
) : BaseSettingsBottomSheet() {

    private lateinit var binding: ModalWeightBinding
    private var currentWeight = initialWeight

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val min = binding.slider.valueFrom
        val max = binding.slider.valueTo
        view.findViewById<TextView>(R.id.tvTitle).text = "Weight"

        currentWeight = when {
            initialWeight < min -> min
            initialWeight > max -> max
            else -> initialWeight
        }

        binding.slider.value = currentWeight
        updateLabel()

        binding.slider.addOnChangeListener { _, value, _ ->
            currentWeight = value
            updateLabel()
        }

        binding.btnSave.setOnClickListener {

            binding.btnSave.text = "Saved"
            binding.btnSave.isEnabled = false
            binding.btnSave.hapticConfirm()

            binding.root.postDelayed({
                listener.onWeightSelected(currentWeight)
                dismiss()
            }, 350)
        }


    }


    private fun updateLabel() {
        binding.tvValue.text = "${currentWeight.toInt()} kg"
    }
}
