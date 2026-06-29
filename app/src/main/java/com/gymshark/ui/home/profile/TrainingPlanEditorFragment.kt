package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingPlanEditorBinding
import com.gymshark.domain.models.DaySlot
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class TrainingPlanEditorFragment :
    BaseFragment<FragmentTrainingPlanEditorBinding>(FragmentTrainingPlanEditorBinding::inflate) {

    private val planVm: TrainingSetsViewModel by activityViewModel()
    private val locale: Locale get() = Locale.getDefault()
    private lateinit var daysAdapter: PlanDayAdapter
    private lateinit var muscleAdapter: MuscleToggleAdapter
    private var currentDays: List<DayOfWeek> = emptyList()
    private var currentSlots: List<DaySlot> = emptyList()
    private var currentCategories: List<String> = emptyList()
    private var activeDay: DayOfWeek? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLists()
        setupActions()
        observeState()
        planVm.resetDraft()
    }

    private fun setupLists() = with(binding) {
        daysAdapter = PlanDayAdapter { day ->
            activeDay = day
            renderEditor()
        }
        daysList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        daysList.adapter = daysAdapter

        muscleAdapter = MuscleToggleAdapter { type -> toggleType(type) }
        muscleList.layoutManager = GridLayoutManager(requireContext(), 2)
        muscleList.adapter = muscleAdapter
    }

    private fun setupActions() = with(binding) {
        btnBack.setOnClickListener { findNavController().navigateUp() }
        btnChooseDays.setOnClickListener { openTrainingDaysSettings() }
        btnClearDay.setOnClickListener {
            activeSlot()?.let { slot -> planVm.clearSlot(slot.id) }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    planVm.baseDays.collectLatest { days ->
                        currentDays = days
                        if (activeDay !in days) {
                            activeDay = days.firstOrNull()
                        }
                        renderMode()
                        renderEditor()
                        planVm.syncWithBase()
                    }
                }

                launch {
                    planVm.displayedSlots.collectLatest { slots ->
                        currentSlots = slots
                        if (activeDay == null) {
                            activeDay = slots.firstOrNull()?.day
                        }
                        renderEditor()
                    }
                }

                launch {
                    planVm.categories.collectLatest { categories ->
                        currentCategories = categories
                        renderMuscles()
                    }
                }

                launch {
                    planVm.saveState.collectLatest { state ->
                        renderSaveState(state)
                    }
                }
            }
        }
    }

    private fun toggleType(type: String) {
        val slot = activeSlot() ?: return
        val updated = if (type in slot.types) {
            slot.types.filterNot { it == type }
        } else {
            slot.types + type
        }
        planVm.setSlotTypes(slot.id, updated)
    }

    private fun renderMode() = with(binding) {
        val hasDays = currentDays.isNotEmpty()
        emptyState.visibility = if (hasDays) View.GONE else View.VISIBLE
        editorContent.visibility = if (hasDays) View.VISIBLE else View.GONE
    }

    private fun renderEditor() {
        renderDays()
        renderActiveDay()
        renderMuscles()
    }

    private fun renderDays() {
        daysAdapter.submitList(
            currentDays.map { day ->
                val slot = currentSlots.firstOrNull { it.day == day }
                PlanDayChip(
                    day = day,
                    selected = day == activeDay,
                    assignedCount = slot?.types.orEmpty().count { it.isNotBlank() }
                )
            }
        )
    }

    private fun renderActiveDay() = with(binding) {
        val day = activeDay
        val slot = activeSlot()
        val assignedTypes = slot?.types.orEmpty().filter { it.isNotBlank() }.distinct()

        tvActiveDay.text = day?.getDisplayName(TextStyle.FULL, locale) ?: "Training day"
        tvActiveSummary.text = when (assignedTypes.size) {
            0 -> "No focus selected"
            1 -> assignedTypes.first()
            else -> assignedTypes.joinToString()
        }
        btnClearDay.alpha = if (assignedTypes.isEmpty()) 0.45f else 1f
        renderAssignedTypes(assignedTypes)
    }

    private fun renderAssignedTypes(types: List<String>) = with(binding.assignedTypesContainer) {
        removeAllViews()

        if (types.isEmpty()) {
            addView(
                TextView(requireContext()).apply {
                    text = "Tap focus areas below to build this day."
                    setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                    textSize = 13f
                    gravity = android.view.Gravity.CENTER_VERTICAL
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            return
        }

        types.forEach { type ->
            addView(
                TextView(requireContext()).apply {
                    text = "$type  x"
                    setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
                    textSize = 12f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    gravity = android.view.Gravity.CENTER
                    background = ContextCompat.getDrawable(context, R.drawable.bg_slot_muscle_chip)
                    setPadding(12.dp(), 0, 12.dp(), 0)
                    setOnClickListener { activeSlot()?.let { slot -> planVm.removeSlotType(slot.id, type) } }
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    34.dp()
                ).apply {
                    marginEnd = 8.dp()
                }
            )
        }
    }

    private fun renderMuscles() {
        val selectedTypes = activeSlot()?.types.orEmpty().toSet()
        muscleAdapter.submitList(
            currentCategories.map { type ->
                MuscleToggleChip(name = type, selected = type in selectedTypes)
            }
        )
    }

    private fun renderSaveState(state: PlanSaveState) = with(binding.tvSaveState) {
        text = when (state) {
            PlanSaveState.Idle -> "Ready"
            PlanSaveState.Saving -> "Saving..."
            PlanSaveState.Saved -> "Saved"
        }
        setTextColor(
            ContextCompat.getColor(
                requireContext(),
                when (state) {
                    PlanSaveState.Idle -> R.color.textSecondary
                    PlanSaveState.Saving -> R.color.trainingWarmup
                    PlanSaveState.Saved -> R.color.trainingStatus
                }
            )
        )
    }

    private fun activeSlot(): DaySlot? =
        currentSlots.firstOrNull { it.day == activeDay }

    private fun openTrainingDaysSettings() {
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.profileSettingsFragment, true)
            .build()
        findNavController().navigate(
            R.id.profileSettingsFragment,
            Bundle().apply {
                putString(
                    ProfileSettingsFragment.ARG_INITIAL_SECTION,
                    ProfileSettingsFragment.SECTION_DAYS
                )
            },
            options
        )
    }

    override fun onDestroyView() {
        binding.daysList.adapter = null
        binding.muscleList.adapter = null
        super.onDestroyView()
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}
