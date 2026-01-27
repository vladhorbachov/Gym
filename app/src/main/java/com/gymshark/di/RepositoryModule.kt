package com.gymshark.di

import com.gymshark.data.auth.AuthRepository
import com.gymshark.data.auth.AuthRepositoryImpl
import com.gymshark.data.exercises.ExerciseRepository
import com.gymshark.data.exercises.ExerciseRepositoryImpl
import com.gymshark.data.settings.SettingsRepository
import com.gymshark.data.settings.SettingsRepositoryImpl
import com.gymshark.data.training.TrainingRepository
import com.gymshark.data.training.TrainingRepositoryImpl
import com.gymshark.data.training.mapper.PrUpdateMapper
import com.gymshark.data.training.mapper.TrainingDraftMapper
import com.gymshark.data.training.seed.ExercisesSeedDataSource
import com.gymshark.data.training.seed.ExercisesSeedDataSourceImpl
import com.gymshark.data.user.UserRepository
import com.gymshark.data.user.UserRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<AuthRepository> { AuthRepositoryImpl(get()) }

    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    single<ExerciseRepository> { ExerciseRepositoryImpl(get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single { TrainingDraftMapper() }

    single { PrUpdateMapper() }

    single<ExercisesSeedDataSource> { ExercisesSeedDataSourceImpl() }

    single<TrainingRepository> {
        TrainingRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            androidContext(),
            get(),
            get(),
            get()
        )
    }
}
