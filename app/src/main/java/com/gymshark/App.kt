package com.gymshark

import android.app.Application
import com.gymshark.di.appModule
import com.gymshark.di.databaseModule
import com.gymshark.di.firebaseModule
import com.gymshark.di.repositoryModule
import com.gymshark.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(firebaseModule, repositoryModule, viewModelModule, databaseModule, appModule)
        }
    }
}