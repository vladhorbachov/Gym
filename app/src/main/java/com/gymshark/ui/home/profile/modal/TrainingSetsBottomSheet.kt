package com.gymshark.ui.home.profile.modal

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.gymshark.R
import com.gymshark.databinding.ModalTrainingSetsBinding
import com.gymshark.ui.home.profile.TrainingSetsViewModel
import com.gymshark.utils.view.hapticConfirm
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class TrainingSetsBottomSheet : BaseSettingsBottomSheet() {

    override val sheetHeightRatio: Float = 0.78f

    private var _binding: ModalTrainingSetsBinding? = null
    private val binding get() = _binding!!

    private val vm: TrainingSetsViewModel by activityViewModel()

    private lateinit var slotsAdapter: TrainingSlotsAdapter
    private lateinit var typesAdapter: TrainingTypesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ModalTrainingSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupSlots()
        setupTypes()
        view.findViewById<TextView>(R.id.tvTitle).text = "Training plan"

        binding.btnAddDay.setOnClickListener {

            if (vm.baseDays.value.isEmpty()) {
                Toast.makeText(requireContext(), "Select training days first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.addNext()
            binding.slotsList.post {
                val lastIndex = slotsAdapter.itemCount - 1
                if (lastIndex >= 0) binding.slotsList.smoothScrollToPosition(lastIndex)
            }
        }


        observeVm()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setupSlots() {
        slotsAdapter = TrainingSlotsAdapter { slot, type ->

            val updated = slot.types.toMutableList()

            if (type !in updated)
                updated.add(type)
            else
                updated.remove(type)

            vm.setSlotTypes(slot.id, updated)
        }


        binding.slotsList.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.slotsList.adapter = slotsAdapter

        binding.btnSavePlan.setOnClickListener {

            binding.btnSavePlan.text = "Saved"
            binding.btnSavePlan.isEnabled = false
            binding.btnSavePlan.hapticConfirm()

            binding.root.postDelayed({
                vm.commitChanges()
                dismiss()
            }, 350)
        }



    }

    private fun setupTypes() {
        typesAdapter = TrainingTypesAdapter()

        binding.typesList.layoutManager =
            GridLayoutManager(requireContext(), 1)

        binding.typesList.adapter = typesAdapter
    }

    private fun observeVm() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.displayedSlots.collectLatest { list ->
                        if (_binding != null) {
                            slotsAdapter.submitList(list)
                        }
                    }
                }

                launch {
                    vm.categories.collectLatest { list ->
                        if (_binding != null) {
                            typesAdapter.submitList(list)
                        }
                    }
                }
            }
        }
    }



    override fun onDestroyView() {
        binding.slotsList.adapter = null
        binding.typesList.adapter = null
        _binding = null
        super.onDestroyView()
    }

}


