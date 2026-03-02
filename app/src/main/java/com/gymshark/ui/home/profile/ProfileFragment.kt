package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.gymshark.R
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.databinding.FragmentProfileBinding
import com.gymshark.domain.models.DaySlot
import com.gymshark.domain.models.Pentagon
import com.gymshark.domain.models.Series
import com.gymshark.ui.home.profile.modal.TrainingDaysBottomSheet
import com.gymshark.ui.home.profile.modal.TrainingDaysResultListener
import com.gymshark.ui.home.profile.modal.TrainingSetsBottomSheet
import com.gymshark.ui.home.profile.modal.WeightBottomSheet
import com.gymshark.ui.home.profile.modal.WeightResultListener
import com.gymshark.ui.home.profile.row.ProfileRowModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileViewModel by viewModel()
    private val trainingSetsVm: TrainingSetsViewModel by activityViewModel()

    private var weight: Float = 0f

    private val locale = Locale.getDefault()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        initRows()
        loadUser()
        observeTrainingSlotsFromDb()
        binding.btnSave.setOnClickListener {

            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).duration = 90
                }

            saveUser()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.plannedDaysFlow.collect { days ->

                    binding.rowTrainingDays.setValue(
                        if (days.isEmpty())
                            "Not selected"
                        else
                            days.joinToString(", ") { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    )
                }
            }
        }

    }

    private fun initRows() {
        binding.rowTrainingDays.bind(ProfileRowModel("Training days", "Not selected"))
        binding.rowTrainingSet.bind(ProfileRowModel("Training set", "Not selected"))
        binding.rowTrainingSet.setOnClickListener {
            vm.setCurrentUserId()

            TrainingSetsBottomSheet().show(parentFragmentManager, "training_sets")
        }
        binding.rowWeight.bind(ProfileRowModel("Weight", "Not set"))

        binding.rowTrainingDays.setOnClickListener {
            vm.setCurrentUserId()

            viewLifecycleOwner.lifecycleScope.launch {
                val currentDays = vm.plannedDaysFlow.value

                TrainingDaysBottomSheet(
                    initialDays = currentDays
                ).show(parentFragmentManager, "training_days")
            }
        }



        binding.rowWeight.setOnClickListener {
            WeightBottomSheet(
                initialWeight = weight,
                listener = object : WeightResultListener {
                    override fun onWeightSelected(weight: Float) {
                        this@ProfileFragment.weight = weight
                        binding.rowWeight.bind(ProfileRowModel("Weight", "${weight.toInt()} kg"))

                        vm.setCurrentUserId()
                        vm.updateWeight(weight)


                        binding.rowWeight.flash()
                    }
                }
            ).show(parentFragmentManager, "weight")
        }
    }



    private fun observeTrainingSlotsFromDb() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.daySlotsFlow.collectLatest { slots ->
                    updateTrainingSetRow(slots)
                }
            }
        }
    }

    private fun loadUser() {
        viewLifecycleOwner.lifecycleScope.launch {

            vm.setCurrentUserId()

            val user = vm.loadUser()

            weight = user?.weight ?: 0f

            binding.rowWeight.bind(ProfileRowModel("Weight", "${weight.toInt()} kg"))
        }
    }


    private fun updateTrainingSetRow(slots: List<DaySlot>) {

        if (slots.isEmpty()) {
            binding.rowTrainingSet.bind(ProfileRowModel("Training set", "Not selected"))
            return
        }

        val grouped = slots.groupBy { it.day }

        val text = grouped.entries
            .sortedBy { it.key.ordinal }
            .joinToString(" · ") { (day, daySlots) ->

                val types = daySlots
                    .flatMap { it.types }
                    .distinct()

                val typesText = if (types.isEmpty()) "-" else types.joinToString("/")

                "${day.getDisplayName(TextStyle.SHORT, locale)} — $typesText"
            }

        binding.rowTrainingSet.bind(ProfileRowModel("Training set", text))
    }


    private fun saveUser() = with(binding) {

        viewLifecycleOwner.lifecycleScope.launch {


            vm.setCurrentUserId()
            val existing = vm.loadUser()

            val user = (existing ?: UserEntity(
                name = "",
                age = 0,
                sex = false,
                weight = 0f,
                height = 0,
                minBPM = 0,
                maxBPM = 0,
                avgBPM = 0,
                pentagon = Pentagon(0,0,0,0,0),
                trainingSlots = emptyList(),
                series = Series(0,0,false)
            )).copy(
                name = etName.text.toString(),
                age = etAge.text.toString().toIntOrNull() ?: 0,
                sex = swSex.isChecked,
                weight = weight,
                height = etHeight.text.toString().toIntOrNull() ?: 0,
                series = Series(
                    current = etCurrentSeries.text.toString().toIntOrNull() ?: 0,
                    maxSeries = etMaxSeries.text.toString().toIntOrNull() ?: 0,
                    isActive = swSeriesActive.isChecked
                )
            )

            vm.saveUser(user)
            Snackbar.make(requireView(), "User saved", Snackbar.LENGTH_SHORT).show()
            binding.btnSave.text = "Saved"

            binding.btnSave.postDelayed({
                binding.btnSave.text = "Save"
            }, 1200)

        }
        requireView().clearFocus()

    }








    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
