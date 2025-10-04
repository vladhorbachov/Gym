package com.gymshark.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.gymshark.R
import com.gymshark.ui.onboarding.adapter.OnboardingAdapter


class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ViewPager2>(R.id.vpOnboarding).adapter =
            OnboardingAdapter(childFragmentManager, lifecycle)
    }

}