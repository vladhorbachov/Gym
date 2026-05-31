package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gymshark.R
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.databinding.FragmentProfileBinding
import com.gymshark.domain.models.DaySlot
import com.gymshark.ui.home.profile.modal.ProfileDetailsBottomSheet
import com.gymshark.ui.home.profile.modal.TrainingDaysBottomSheet
import com.gymshark.ui.home.profile.modal.TrainingSetsBottomSheet
import com.gymshark.ui.home.profile.modal.WeightBottomSheet
import com.gymshark.ui.home.profile.modal.WeightResultListener
import com.gymshark.ui.home.profile.row.ProfileRowModel
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.format.TextStyle
import java.util.Locale

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val vm: ProfileViewModel by viewModel()
    private val locale = Locale.getDefault()
    private var currentUser: UserEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        observeUserState()
        observeWorkoutSetup()
        vm.refreshSeriesState()
    }

    private fun setupActions() = with(binding) {
        ivAvatar.setOnClickListener { openPersonalDetails() }
        tvName.setOnClickListener { openPersonalDetails() }
        tvProfileSummary.setOnClickListener { openPersonalDetails() }

        rowPersonalData.setOnClickListener { openPersonalDetails() }
        rowTrainingDays.setOnClickListener { openTrainingDays() }
        rowTrainingSet.setOnClickListener { openTrainingPlan() }
        rowWeight.setOnClickListener { openWeight() }
    }

    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.observeUser().collectLatest { user ->
                        currentUser = user
                        bindUser(user)
                    }
                }
            }
        }
    }

    private fun observeWorkoutSetup() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.plannedDaysFlow.collectLatest { days ->
                        binding.rowTrainingDays.bind(
                            ProfileRowModel(
                                "Training days",
                                if (days.isEmpty()) "Not selected" else formatDays(days)
                            )
                        )
                    }
                }

                launch {
                    vm.daySlotsFlow.collectLatest { slots ->
                        binding.rowTrainingSet.bind(
                            ProfileRowModel(
                                "Training plan",
                                formatTrainingSlots(slots)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun bindUser(user: UserEntity?) = with(binding) {
        val safeName = user?.name?.takeIf { it.isNotBlank() } ?: "Your profile"
        val summary = buildList {
            user?.age?.takeIf { it > 0 }?.let { add("$it y.o.") }
            user?.height?.takeIf { it > 0 }?.let { add("$it cm") }
            add(if (user?.sex == true) "Male" else "Female")
        }.joinToString(" / ")

        tvName.text = safeName
        tvProfileSummary.text = summary
        tvVisitSeries.text = "Current streak\n${user?.series?.current ?: 0}"
        tvVisitMaxSeries.text = "Best streak\n${user?.series?.maxSeries ?: 0}"

        rowPersonalData.bind(
            ProfileRowModel(
                "Personal details",
                summary.ifBlank { "Not set" }
            )
        )
        rowWeight.bind(
            ProfileRowModel(
                "Weight",
                user?.weight?.takeIf { it > 0f }?.let { "${it.toInt()} kg" } ?: "Not set"
            )
        )
    }

    private fun formatDays(days: Set<java.time.DayOfWeek>): String =
        days.joinToString(", ") {
            it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        }

    private fun formatTrainingSlots(slots: List<DaySlot>): String {
        if (slots.isEmpty()) return "Not selected"

        val grouped = slots.groupBy { it.day }
        return grouped.entries
            .sortedBy { it.key.ordinal }
            .joinToString(" / ") { (day, daySlots) ->
                val types = daySlots.flatMap { it.types }.distinct()
                val typesText = if (types.isEmpty()) "Not set" else types.joinToString("/")
                "${day.getDisplayName(TextStyle.SHORT, locale)}: $typesText"
            }
    }

    private fun openPersonalDetails() {
        ProfileDetailsBottomSheet().show(parentFragmentManager, "profile_details")
    }

    private fun openTrainingDays() {
        TrainingDaysBottomSheet(
            initialDays = vm.plannedDaysFlow.value
        ).show(parentFragmentManager, "training_days")
    }

    private fun openTrainingPlan() {
        TrainingSetsBottomSheet().show(parentFragmentManager, "training_sets")
    }

    private fun openWeight() {
        WeightBottomSheet(
            initialWeight = currentUser?.weight ?: 0f,
            listener = object : WeightResultListener {
                override fun onWeightSelected(weight: Float) {
                    vm.updateWeight(weight)
                    vm.registerActivity(System.currentTimeMillis())
                }
            }
        ).show(parentFragmentManager, "weight")
    }
}
