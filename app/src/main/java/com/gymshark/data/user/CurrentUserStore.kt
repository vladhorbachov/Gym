package com.gymshark.data.user

import com.gymshark.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow

class CurrentUserStore(
    private val prefs: UserPrefs
) {
    private val currentUserIdState = MutableStateFlow(prefs.getCurrentUserId())

    val currentUserIdFlow: Flow<String?> = currentUserIdState

    suspend fun setCurrentUserId(id: String) {
        prefs.saveCurrentUserId(id)
        currentUserIdState.value = id
    }

    suspend fun clear() {
        prefs.clearCurrentUserId()
        currentUserIdState.value = null
    }

    fun currentUserIdOrNull(): String? = currentUserIdState.value
}
