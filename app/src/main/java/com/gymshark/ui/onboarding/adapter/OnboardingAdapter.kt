package com.gymshark.ui.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gymshark.ui.onboarding.OnboardingFragmentOne
import com.gymshark.ui.onboarding.OnboardingFragmentThree
import com.gymshark.ui.onboarding.OnboardingFragmentTwo

class OnboardingAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val fList = listOf(
        OnboardingFragmentOne(),
        OnboardingFragmentTwo(),
        OnboardingFragmentThree()
    )

    override fun createFragment(position: Int): Fragment {
        return fList[position]
    }

    override fun getItemCount(): Int {
        return fList.size
    }
}