package com.gymshark.ui.home.profile.modal

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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

        map.forEach { (day, tile) ->
            renderDayTile(tile, selectedDays.contains(day))

            tile.setOnClickListener { v ->
                v.hapticTick()

                if (selectedDays.contains(day)) selectedDays.remove(day)
                else selectedDays.add(day)

                renderDayTile(tile, selectedDays.contains(day))
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(90)
                    .withEndAction {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(110).start()
                    }
                    .start()

                updateSaveState()
            }
        }
    }

    private fun renderDayTile(tile: View, selected: Boolean) {
        val background = if (selected) R.drawable.bg_day_tile_selected else R.drawable.bg_day_tile_default
        tile.background = ContextCompat.getDrawable(tile.context, background)
        tile.isSelected = selected
        val nameColor = if (selected) R.color.trainingOnAction else R.color.textPrimary
        val captionColor = if (selected) R.color.trainingOnAction else R.color.textSecondary
        setTextColors(tile, nameColor, captionColor)
    }

    private fun setTextColors(view: View, nameColor: Int, captionColor: Int) {
        if (view is TextView) {
            val color = if (view.text.length <= 3) nameColor else captionColor
            view.setTextColor(ContextCompat.getColor(view.context, color))
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setTextColors(view.getChildAt(i), nameColor, captionColor)
            }
        }
    }



    private fun updateSaveState() {
        binding.btnSave.isEnabled = selectedDays.isNotEmpty()
    }
}
