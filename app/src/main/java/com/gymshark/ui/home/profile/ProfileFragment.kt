package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModel()

    private val calendarDaysOrder = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    )

    private val selectedDays =
        mutableSetOf<Int>()
    private val locale: Locale get() = Locale.getDefault()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            val user = vm.loadUser(binding.etUserId.text.toString())
            selectedDays.clear()
            user?.trainingDay?.days?.forEach { selectedDays.add(it) }
            renderSelectedDays()
        }

        binding.btnPickDays.setOnClickListener { showDayPicker() }
        binding.btnSave.setOnClickListener { saveUser() }
        binding.btnPickTrainingSet.setOnClickListener { showSetPicker() }
    }

    private fun renderSelectedDays() {
        binding.tvSelectedDays.text =
            if (selectedDays.isEmpty()) "Selected days: none"
            else "Selected days: " + selectedDays
                .sortedBy { calendarDaysOrder.indexOf(it).takeIf { i -> i >= 0 } ?: 99 }
                .joinToString { calendarLabel(it) }
    }

    private fun showDayPicker() {
        val labels = calendarDaysOrder.map { calendarLabel(it) }.toTypedArray()
        val checked = BooleanArray(calendarDaysOrder.size) { idx ->
            selectedDays.contains(calendarDaysOrder[idx])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pick training days")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                val calValue = calendarDaysOrder[which]
                if (isChecked) selectedDays.add(calValue) else selectedDays.remove(calValue)
            }
            .setPositiveButton("OK") { d, _ -> renderSelectedDays(); d.dismiss() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetPicker() {
        findNavController().navigate(R.id.action_navProfile_to_trainingSetsFragment)
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
            trainingDay = TrainingDay(selectedDays.sorted().toMutableList()),
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

    private fun calendarLabel(calendarValue: Int): String =
        when (calendarValue) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> null
        }?.getDisplayName(TextStyle.SHORT, locale) ?: "?"
}
