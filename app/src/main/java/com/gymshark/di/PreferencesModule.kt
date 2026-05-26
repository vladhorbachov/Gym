package com.gymshark.di

import com.gymshark.data.prefs.UserPrefs
import com.gymshark.data.user.CurrentUserStore
import org.koin.dsl.module

val prefsModule = module {
    single { UserPrefs(get()) }
    single { CurrentUserStore(get()) }
}
