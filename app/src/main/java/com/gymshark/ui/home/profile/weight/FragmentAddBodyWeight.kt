package com.gymshark.ui.home.profile.weight

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gymshark.databinding.FragmentWeightBottomSheetBinding
import com.gymshark.ui.home.profile.ProfileViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FragmentAddBodyWeight : BottomSheetDialogFragment() {

    private var _binding: FragmentWeightBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val text = p0?.toString() ?: ""
                if (text.contains(",")) {
                    val newText = text.replace(",", ".")
                    binding.etWeight.setText(newText)
                }
            }

            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {

            }

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

        })
        binding.btnSave.setOnClickListener {
            saveWeight()
        }

    }

    private fun saveWeight() {
        val weight = binding.etWeight.text
            ?.toString()
            ?.replace(",", ".")
            ?.toFloatOrNull()

        if (weight != null && weight in 20f..300f) {
            viewModel.saveBodyWeight(weight)
            dismiss()
        } else {
            binding.etWeight.error = "Invalid weight"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
