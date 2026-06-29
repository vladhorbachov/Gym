package com.gymshark.ui.home.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentProfileBinding
import com.gymshark.ui.home.profile.row.ProfileRowModel
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val vm: ProfileViewModel by activityViewModel()
    private val locale: Locale get() = Locale.getDefault()
    private var currentState = ProfileState()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActions()
        observeProfileState()
        vm.refreshSeriesState()
    }

    private fun setupActions() = with(binding) {
        ivAvatar.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_PERSONAL) }
        tvName.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_PERSONAL) }
        tvProfileSummary.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_PERSONAL) }

        rowPersonalData.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_PERSONAL) }
        rowTrainingDays.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_DAYS) }
        rowTrainingSet.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_PLAN) }
        rowWeight.setOnClickListener { openSettings(ProfileSettingsFragment.SECTION_WEIGHT) }
    }

    private fun observeProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.profileState.collectLatest { state ->
                    currentState = state
                    renderProfile(state)
                }
            }
        }
    }

    private fun renderProfile(state: ProfileState) = with(binding) {
        val user = state.user
        val detailsPreview = ProfilePreviewFormatter.personalDetails(user)

        tvName.text = user?.name?.takeIf { it.isNotBlank() } ?: "Your profile"
        tvProfileSummary.text = detailsPreview.takeUnless { it == "Not set" } ?: "Set up your profile"
        tvVisitSeries.text = "Current streak\n${user?.series?.current ?: 0}"
        tvVisitMaxSeries.text = "Best streak\n${user?.series?.maxSeries ?: 0}"

        rowPersonalData.bind(
            ProfileRowModel(
                title = "Personal details",
                value = detailsPreview
            )
        )
        rowTrainingDays.bind(
            ProfileRowModel(
                title = "Training days",
                value = ProfilePreviewFormatter.trainingDays(state.selectedDays, locale)
            )
        )
        rowTrainingSet.bind(
            ProfileRowModel(
                title = "Training plan",
                value = ProfilePreviewFormatter.trainingPlan(
                    state.trainingSlots,
                    state.selectedDays,
                    locale
                )
            )
        )
        rowWeight.bind(
            ProfileRowModel(
                title = "Weight",
                value = ProfilePreviewFormatter.weight(user)
            )
        )
    }

    private fun openSettings(section: String) {
        findNavController().navigate(
            R.id.action_navProfile_to_profileSettingsFragment,
            Bundle().apply { putString(ProfileSettingsFragment.ARG_INITIAL_SECTION, section) }
        )
    }
}
