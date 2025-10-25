package com.gymshark.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CurrentUserStore(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
    }

    val currentUserIdFlow: Flow<String?> =
        dataStore.data.map { it[Keys.CURRENT_USER_ID] }

    suspend fun setCurrentUserId(id: String) {
        dataStore.edit { it[Keys.CURRENT_USER_ID] = id }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(Keys.CURRENT_USER_ID) }
    }

    suspend fun currentUserIdOrNull(): String? = currentUserIdFlow.first()
}
