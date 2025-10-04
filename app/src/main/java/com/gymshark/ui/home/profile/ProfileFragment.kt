package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.gymshark.R
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.models.Pentagon
import com.gymshark.data.models.Series
import com.gymshark.data.models.TrainingDay
import com.gymshark.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModel()

    private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val selectedDays = mutableSetOf<Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        binding.btnPickDays.setOnClickListener { showDayPicker() }
        binding.btnSave.setOnClickListener { saveUser() }
    }

    private fun showDayPicker() {
        val checkedItems = BooleanArray(daysOfWeek.size) { selectedDays.contains(it) }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pick training days")
            .setMultiChoiceItems(daysOfWeek.toTypedArray(), checkedItems) { _, which, isChecked ->
                if (isChecked) selectedDays.add(which) else selectedDays.remove(which)
            }
            .setPositiveButton("OK") { dialog, _ ->
                binding.tvSelectedDays.text =
                    if (selectedDays.isEmpty()) "Selected days: none"
                    else "Selected days: " + selectedDays.joinToString { daysOfWeek[it] }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUser() = with(binding) {
        val user = UserEntity(
            userId = etUserId.text.toString(),
            name = etName.text.toString(),
            age = etAge.text.toString().toIntOrNull() ?: 0,
            sex = swSex.isChecked,
            weight = etWeight.text.toString().toFloatOrNull() ?: 0f,
            height = etHeight.text.toString().toIntOrNull() ?: 0,
            minBPM = etMinBpm.text.toString().toIntOrNull() ?: 0,
            maxBPM = etMaxBpm.text.toString().toIntOrNull() ?: 0,
            avgBPM = etAvgBpm.text.toString().toIntOrNull() ?: 0,
            pentagon = Pentagon(
                strength = etStrength.text.toString().toIntOrNull() ?: 0,
                power = etPower.text.toString().toIntOrNull() ?: 0,
                agility = etAgility.text.toString().toIntOrNull() ?: 0,
                endurance = etEndurance.text.toString().toIntOrNull() ?: 0,
                mobility = etMobility.text.toString().toIntOrNull() ?: 0,
            ),
            trainingDay = TrainingDay(selectedDays.toMutableList()),
            series = Series(
                current = etCurrentSeries.text.toString().toIntOrNull() ?: 0,
                maxSeries = etMaxSeries.text.toString().toIntOrNull() ?: 0,
                isActive = swSeriesActive.isChecked
            )
        )

        viewLifecycleOwner.lifecycleScope.launch {
            vm.saveUser(user)
            Snackbar.make(requireView(), "User saved", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
