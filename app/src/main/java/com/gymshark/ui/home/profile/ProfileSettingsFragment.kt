package com.gymshark.ui.home.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentProfileSettingsBinding
import com.gymshark.utils.BaseFragment
import com.gymshark.utils.view.hapticConfirm
import com.gymshark.utils.view.hapticTick
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.DayOfWeek
import java.util.Locale
import kotlin.math.roundToInt

class ProfileSettingsFragment :
    BaseFragment<FragmentProfileSettingsBinding>(FragmentProfileSettingsBinding::inflate) {

    private val vm: ProfileViewModel by activityViewModel()
    private val locale: Locale get() = Locale.getDefault()

    private var targetSection = SECTION_PERSONAL
    private var activeSection = SECTION_PERSONAL
    private var bindingState = false
    private var personalDirty = false
    private var daysDirty = false
    private var weightDirty = false
    private var isMale = true
    private val selectedDays = mutableSetOf<DayOfWeek>()
    private var currentWeight = DEFAULT_WEIGHT
    private var updatingWeightInput = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetSection = arguments?.getString(ARG_INITIAL_SECTION) ?: SECTION_PERSONAL
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        setupWeightEditor()
        observeProfileState()
    }

    private fun setupActions() = with(binding) {
        btnBack.setOnClickListener { findNavController().navigateUp() }
        tabPersonal.setOnClickListener { selectSection(SECTION_PERSONAL) }
        tabDays.setOnClickListener { selectSection(SECTION_DAYS) }
        tabPlan.setOnClickListener { selectSection(SECTION_PLAN) }
        tabWeight.setOnClickListener { selectSection(SECTION_WEIGHT) }
        selectSection(targetSection)

        btnFemale.setOnClickListener {
            personalDirty = true
            isMale = false
            renderGender()
            it.hapticTick()
        }
        btnMale.setOnClickListener {
            personalDirty = true
            isMale = true
            renderGender()
            it.hapticTick()
        }

        listOf(etName, etAge, etHeight).forEach { field ->
            field.addTextChangedListener(simpleWatcher {
                if (!bindingState) personalDirty = true
            })
        }

        dayViews().forEach { (day, tile) ->
            tile.setOnClickListener {
                daysDirty = true
                if (day in selectedDays) selectedDays.remove(day) else selectedDays.add(day)
                renderDayTile(tile, day in selectedDays)
                tvDaysPreview.text = ProfilePreviewFormatter.trainingDays(selectedDays, locale)
                it.hapticTick()
            }
        }

        btnSavePersonal.setOnClickListener { savePersonal() }
        btnSaveDays.setOnClickListener { saveDays() }
        btnEditPlan.setOnClickListener {
            findNavController().navigate(R.id.action_profileSettingsFragment_to_trainingPlanEditorFragment)
        }
        btnSaveWeight.setOnClickListener { saveWeight() }
    }

    private fun setupWeightEditor() = with(binding) {
        sliderWeight.valueFrom = MIN_WEIGHT
        sliderWeight.valueTo = MAX_WEIGHT
        sliderWeight.stepSize = 1f
        sliderWeight.addOnChangeListener { _, value, fromUser ->
            if (fromUser && !bindingState) {
                weightDirty = true
                setWeight(value, updateInput = true)
            }
        }

        etWeight.addTextChangedListener(simpleWatcher {
            if (updatingWeightInput || bindingState) return@simpleWatcher
            weightDirty = true
            weightLayout.error = null
            etWeight.text?.toString()?.toFloatOrNull()?.let { value ->
                if (value in MIN_WEIGHT..MAX_WEIGHT) setWeight(value, updateInput = false)
            }
        })

        btnWeightMinus.setOnClickListener {
            weightDirty = true
            setWeight((currentWeight - 1f).coerceAtLeast(MIN_WEIGHT), updateInput = true)
            it.hapticTick()
        }
        btnWeightPlus.setOnClickListener {
            weightDirty = true
            setWeight((currentWeight + 1f).coerceAtMost(MAX_WEIGHT), updateInput = true)
            it.hapticTick()
        }
    }

    private fun observeProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.profileState.collectLatest { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: ProfileState) = with(binding) {
        tvPersonalPreview.text = ProfilePreviewFormatter.personalDetails(state.user)
        tvDaysPreview.text = ProfilePreviewFormatter.trainingDays(state.selectedDays, locale)
        tvPlanPreview.text = ProfilePreviewFormatter.trainingPlan(
            state.trainingSlots,
            state.selectedDays,
            locale
        )

        if (!personalDirty) {
            bindingState = true
            etName.setText(state.user?.name.orEmpty())
            etAge.setText(state.user?.age?.takeIf { it > 0 }?.toString().orEmpty())
            etHeight.setText(state.user?.height?.takeIf { it > 0 }?.toString().orEmpty())
            isMale = state.user?.sex ?: true
            renderGender()
            bindingState = false
        }

        if (!daysDirty) {
            selectedDays.clear()
            selectedDays += state.selectedDays
            dayViews().forEach { (day, tile) -> renderDayTile(tile, day in selectedDays) }
        }

        if (!weightDirty) {
            bindingState = true
            val savedWeight = state.user?.weight?.takeIf { it > 0f } ?: DEFAULT_WEIGHT
            setWeight(savedWeight.coerceIn(MIN_WEIGHT, MAX_WEIGHT), updateInput = true)
            bindingState = false
        }
    }

    private fun savePersonal() = with(binding) {
        ageLayout.error = null
        heightLayout.error = null

        val ageText = etAge.text?.toString().orEmpty().trim()
        val heightText = etHeight.text?.toString().orEmpty().trim()
        val age = ageText.toIntOrNull() ?: 0
        val height = heightText.toIntOrNull() ?: 0

        var valid = true
        if (ageText.isNotEmpty() && age !in 1..120) {
            ageLayout.error = "Use 1-120"
            valid = false
        }
        if (heightText.isNotEmpty() && height !in 80..250) {
            heightLayout.error = "Use 80-250 cm"
            valid = false
        }
        if (!valid) return

        vm.savePersonalDetails(
            name = etName.text?.toString().orEmpty().trim(),
            age = age,
            sex = isMale,
            height = height
        )
        personalDirty = false
        btnSavePersonal.hapticConfirm()
    }

    private fun saveDays() {
        vm.saveTrainingDays(selectedDays.toSet())
        daysDirty = false
        binding.btnSaveDays.hapticConfirm()
    }

    private fun saveWeight() = with(binding) {
        weightLayout.error = null
        val enteredWeight = etWeight.text?.toString()?.toFloatOrNull()
        if (enteredWeight == null || enteredWeight !in MIN_WEIGHT..MAX_WEIGHT) {
            weightLayout.error = "Use ${MIN_WEIGHT.toInt()}-${MAX_WEIGHT.toInt()} kg"
            return
        }

        vm.saveWeight(enteredWeight.roundToInt().toFloat())
        weightDirty = false
        btnSaveWeight.hapticConfirm()
    }

    private fun renderGender() = with(binding) {
        renderSegment(btnFemale, selected = !isMale)
        renderSegment(btnMale, selected = isMale)
    }

    private fun renderSegment(view: TextView, selected: Boolean) {
        view.background = ContextCompat.getDrawable(
            view.context,
            if (selected) R.drawable.bg_profile_settings_tab_selected else R.drawable.bg_profile_settings_tab
        )
        view.setTextColor(
            ContextCompat.getColor(
                view.context,
                if (selected) R.color.trainingAction else R.color.textSecondary
            )
        )
    }

    private fun renderDayTile(tile: TextView, selected: Boolean) {
        renderSegment(tile, selected)
    }

    private fun setWeight(value: Float, updateInput: Boolean) = with(binding) {
        currentWeight = value.coerceIn(MIN_WEIGHT, MAX_WEIGHT).roundToInt().toFloat()
        tvWeightValue.text = "${currentWeight.toInt()} kg"
        if (sliderWeight.value != currentWeight) sliderWeight.value = currentWeight
        if (updateInput && etWeight.text?.toString() != currentWeight.toInt().toString()) {
            updatingWeightInput = true
            etWeight.setText(currentWeight.toInt().toString())
            etWeight.setSelection(etWeight.text?.length ?: 0)
            updatingWeightInput = false
        }
    }

    private fun selectSection(section: String) = with(binding) {
        activeSection = section
        personalSection.visibility = if (section == SECTION_PERSONAL) View.VISIBLE else View.GONE
        daysSection.visibility = if (section == SECTION_DAYS) View.VISIBLE else View.GONE
        planSection.visibility = if (section == SECTION_PLAN) View.VISIBLE else View.GONE
        weightSection.visibility = if (section == SECTION_WEIGHT) View.VISIBLE else View.GONE

        renderTab(tabPersonal, section == SECTION_PERSONAL)
        renderTab(tabDays, section == SECTION_DAYS)
        renderTab(tabPlan, section == SECTION_PLAN)
        renderTab(tabWeight, section == SECTION_WEIGHT)

        profileSettingsScroll.post { profileSettingsScroll.smoothScrollTo(0, 0) }
    }

    private fun renderTab(tab: TextView, selected: Boolean) {
        tab.background = ContextCompat.getDrawable(
            tab.context,
            if (selected) R.drawable.bg_profile_settings_tab_selected else R.drawable.bg_profile_settings_tab
        )
        tab.setTextColor(
            ContextCompat.getColor(
                tab.context,
                if (selected) R.color.trainingAction else R.color.textSecondary
            )
        )
    }

    private fun dayViews(): Map<DayOfWeek, TextView> = with(binding) {
        mapOf(
            DayOfWeek.MONDAY to chipMon,
            DayOfWeek.TUESDAY to chipTue,
            DayOfWeek.WEDNESDAY to chipWed,
            DayOfWeek.THURSDAY to chipThu,
            DayOfWeek.FRIDAY to chipFri,
            DayOfWeek.SATURDAY to chipSat,
            DayOfWeek.SUNDAY to chipSun
        )
    }

    private fun simpleWatcher(afterChanged: () -> Unit): TextWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = afterChanged()
        }

    companion object {
        const val ARG_INITIAL_SECTION = "initialSection"
        const val SECTION_PERSONAL = "personal"
        const val SECTION_DAYS = "days"
        const val SECTION_PLAN = "plan"
        const val SECTION_WEIGHT = "weight"
        private const val DEFAULT_WEIGHT = 70f
        private const val MIN_WEIGHT = 35f
        private const val MAX_WEIGHT = 200f
    }
}
