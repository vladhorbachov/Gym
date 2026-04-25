package com.gymshark.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gymshark.data.prefs.UserPrefs
import com.gymshark.data.user.CurrentUserStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("app_prefs") }
        )
    }
    single { CurrentUserStore(get()) }
}
val prefsModule = module {

    single { UserPrefs(get()) }

    single<String?> {
        get<UserPrefs>().getCurrentUserId()
    }
}