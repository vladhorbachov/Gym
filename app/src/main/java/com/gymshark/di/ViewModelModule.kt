package com.gymshark.di


import com.gymshark.ui.auth.AuthViewModel
import com.gymshark.ui.home.HomeViewModel
import com.gymshark.ui.home.profile.ProfileViewModel
import com.gymshark.ui.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SplashViewModel(get(),get()) }
}
