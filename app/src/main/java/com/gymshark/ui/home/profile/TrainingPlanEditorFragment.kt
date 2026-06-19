package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingPlanEditorBinding
import com.gymshark.domain.models.DaySlot
import com.gymshark.ui.home.profile.modal.TrainingSlotsAdapter
import com.gymshark.ui.home.profile.modal.TrainingTypesAdapter
import com.gymshark.utils.BaseFragment
import com.gymshark.utils.view.hapticConfirm
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.DayOfWeek
import java.util.Locale

class TrainingPlanEditorFragment :
    BaseFragment<FragmentTrainingPlanEditorBinding>(FragmentTrainingPlanEditorBinding::inflate) {

    private val planVm: TrainingSetsViewModel by activityViewModel()
    private val locale: Locale get() = Locale.getDefault()
    private lateinit var slotsAdapter: TrainingSlotsAdapter
    private lateinit var typesAdapter: TrainingTypesAdapter
    private var currentDays: List<DayOfWeek> = emptyList()
    private var currentSlots: List<DaySlot> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLists()
        setupActions()
        observeState()
        planVm.resetDraft()
    }

    private fun setupLists() = with(binding) {
        slotsAdapter = TrainingSlotsAdapter(
            onTypeDropped = { slot, type -> planVm.setSlotType(slot.id, type) },
            onClearSlot = { slot -> planVm.clearSlot(slot.id) },
            onRemoveType = { slot, type -> planVm.removeSlotType(slot.id, type) }
        )
        slotsList.layoutManager = GridLayoutManager(requireContext(), 2)
        slotsList.adapter = slotsAdapter

        typesAdapter = TrainingTypesAdapter()
        typesList.layoutManager = GridLayoutManager(requireContext(), 2)
        typesList.adapter = typesAdapter
        typesList.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> view.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> view.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun setupActions() = with(binding) {
        btnBack.setOnClickListener { findNavController().navigateUp() }
        btnChooseDays.setOnClickListener { openTrainingDaysSettings() }
        btnSavePlan.setOnClickListener {
            planVm.commitChanges()
            btnSavePlan.hapticConfirm()
            findNavController().navigateUp()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    planVm.baseDays.collectLatest { days ->
                        currentDays = days
                        renderMode()
                        planVm.syncWithBase()
                    }
                }

                launch {
                    planVm.displayedSlots.collectLatest { slots ->
                        currentSlots = slots
                        slotsAdapter.submitList(slots)
                        renderPreview()
                    }
                }

                launch {
                    planVm.categories.collectLatest { categories ->
                        typesAdapter.submitList(categories)
                    }
                }
            }
        }
    }

    private fun renderMode() = with(binding) {
        val hasDays = currentDays.isNotEmpty()
        emptyState.visibility = if (hasDays) View.GONE else View.VISIBLE
        editorContent.visibility = if (hasDays) View.VISIBLE else View.GONE
        renderPreview()
    }

    private fun renderPreview() {
        binding.tvPlanPreview.text = ProfilePreviewFormatter.trainingPlan(
            currentSlots,
            currentDays.toSet(),
            locale
        )
    }

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
        binding.slotsList.adapter = null
        binding.typesList.adapter = null
        super.onDestroyView()
    }
}
