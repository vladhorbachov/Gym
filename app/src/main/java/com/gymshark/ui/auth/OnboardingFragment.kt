package com.gymshark.ui.auth

import android.os.Bundle
import android.view.View
import com.gymshark.databinding.FragmentOnboardingBinding
import com.gymshark.ui.onboarding.adapter.OnboardingAdapter
import com.gymshark.utils.BaseFragment

class OnboardingFragment : BaseFragment<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vpOnboarding.adapter = OnboardingAdapter(childFragmentManager, lifecycle)
    }
}
