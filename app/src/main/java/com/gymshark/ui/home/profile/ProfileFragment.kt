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
import com.gymshark.data.models.getListDays
import com.gymshark.data.models.toDaysSlot
import com.gymshark.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModel()

    private val dayOrder = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

    private val selectedDays = mutableSetOf<DayOfWeek>()
    private val locale: Locale get() = Locale.getDefault()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            val user = vm.loadUser(binding.etUserId.text.toString())
            selectedDays.clear()
            val days: List<DayOfWeek> = user?.trainingSlots
                ?.getListDays()
                ?.filterNotNull()
                .orEmpty()
            selectedDays.addAll(days)
            renderSelectedDays()
        }


        binding.btnPickDays.setOnClickListener { showDayPicker() }
        binding.btnSave.setOnClickListener { saveUser() }
        binding.btnPickTrainingSet.setOnClickListener { showSetPicker() }
    }

    private fun renderSelectedDays() {
        val weekOrder = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        )

        val cleaned = selectedDays.filterNotNull().distinct()
        val sorted = cleaned.sortedBy { d ->
            weekOrder.indexOf(d).let { if (it >= 0) it else Int.MAX_VALUE }
        }

        binding.tvSelectedDays.text =
            if (sorted.isEmpty()) "Selected days: none"
            else "Selected days: " + sorted.joinToString { dayLabel(it) }
    }


    private fun showDayPicker() {
        val labels = dayOrder.map { it.getDisplayName(TextStyle.SHORT, locale) }.toTypedArray()
        val checked = BooleanArray(dayOrder.size) { selectedDays.contains(dayOrder[it]) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pick training days")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                val d = dayOrder[which]
                if (isChecked) selectedDays.add(d) else selectedDays.remove(d)
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
            // важливо: toDaysSlot() має приймати List<DayOfWeek>
            trainingSlots = selectedDays.sorted().toDaysSlot(),
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

    private fun dayLabel(d: DayOfWeek): String =
        d.getDisplayName(TextStyle.SHORT, locale)
}
