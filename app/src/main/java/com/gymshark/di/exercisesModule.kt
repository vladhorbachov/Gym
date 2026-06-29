package com.gymshark.di

import com.gymshark.data.exercises.ExercisesCatalog
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val exercisesModule = module {
    single { ExercisesCatalog(androidContext()) }
}
