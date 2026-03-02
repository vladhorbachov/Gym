package com.gymshark.ui.home.profile.modal

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.gymshark.R
import com.gymshark.databinding.ModalTrainingDaysBinding
import com.gymshark.ui.home.profile.ProfileViewModel
import com.gymshark.utils.view.hapticConfirm
import com.gymshark.utils.view.hapticTick
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.DayOfWeek

class TrainingDaysBottomSheet(
    private val initialDays: Set<DayOfWeek>
) : BaseSettingsBottomSheet() {
    private val vm: ProfileViewModel by activityViewModel()
    private lateinit var binding: ModalTrainingDaysBinding
    private val selectedDays = initialDays.toMutableSet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalTrainingDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupDays()
        updateSaveState()
        view.findViewById<TextView>(R.id.tvTitle).text = "Training days"
        binding.btnSave.hapticConfirm()
        binding.btnSave.setOnClickListener {
            binding.btnSave.text = "Saved"
            binding.btnSave.isEnabled = false

            binding.root.postDelayed({
                vm.saveTrainingDays(selectedDays)
                dismiss()
            }, 350)

        }
    }

    private fun setupDays() = with(binding) {

        val map = mapOf(
            DayOfWeek.MONDAY to chipMon,
            DayOfWeek.TUESDAY to chipTue,
            DayOfWeek.WEDNESDAY to chipWed,
            DayOfWeek.THURSDAY to chipThu,
            DayOfWeek.FRIDAY to chipFri,
            DayOfWeek.SATURDAY to chipSat,
            DayOfWeek.SUNDAY to chipSun
        )

        map.forEach { (day, chip) ->

            chip.isChecked = selectedDays.contains(day)

            chip.setOnCheckedChangeListener { v, isChecked ->
                v.hapticTick()

                if (isChecked) selectedDays.add(day)
                else selectedDays.remove(day)

                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(80)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).duration = 80
                    }

                updateSaveState()
            }

        }
    }



    private fun updateSaveState() {
        binding.btnSave.isEnabled = selectedDays.isNotEmpty()
    }
}
