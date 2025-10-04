package com.gymshark.di

import androidx.room.Room
import com.gymshark.data.db.DataBase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            DataBase::class.java,
            "GymDb.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        get<DataBase>().userDao()
    }
    single {
        get<DataBase>().exercisesDao()
    }
}