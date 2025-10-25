package com.gymshark.di

import com.gymshark.data.auth.AuthRepository
import com.gymshark.data.auth.AuthRepositoryImpl
import com.gymshark.data.auth.ExerciseRepository
import com.gymshark.data.auth.ExerciseRepositoryImpl
import com.gymshark.data.auth.SettingsRepository
import com.gymshark.data.auth.SettingsRepositoryImpl
import com.gymshark.data.auth.UserRepository
import com.gymshark.data.auth.UserRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(),get()) }
    single<ExerciseRepository> { ExerciseRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}
