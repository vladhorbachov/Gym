package com.gymshark.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.prefs.UserPrefs
import com.gymshark.data.user.UserRepository
import com.gymshark.domain.models.Pentagon
import com.gymshark.domain.models.Series
import com.gymshark.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository,
    private val userRepository: UserRepository,
    private val prefs: UserPrefs
) : ViewModel() {

    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>()
    val loggedIn = MutableLiveData(repo.isLoggedIn())

    fun register(email: String, password: String) = runOp {
        val result = repo.register(email, password)
        val uid = result.user?.uid ?: error("Registration succeeded but UID is null")
        createLocalUser(uid, email)
    }

    fun login(email: String, password: String) = runOp {
        val result = repo.login(email, password)
        val uid = result.user?.uid ?: error("Login succeeded but UID is null")
        val resolvedEmail = result.user?.email ?: email
        // On a fresh install the local DB is empty even though Firebase still
        // recognises the user, so we recreate the row if it is missing.
        if (userRepository.getById(uid) == null) {
            createLocalUser(uid, resolvedEmail)
        } else {
            syncStoredUserId(uid)
        }
    }

    // Called by RegisterFragment after Firebase credential sign-in succeeds
    // for the Google path, where the Fragment holds the credential result.
    fun handleGoogleSignIn(uid: String, email: String) = runOp {
        if (userRepository.getById(uid) == null) {
            createLocalUser(uid, email)
        } else {
            syncStoredUserId(uid)
        }
    }

    fun signOut() {
        repo.signOut()
        loggedIn.value = false
    }

    // Creates the UserEntity row in Room and persists the uid in both
    // SharedPreferences and DataStore so every downstream repository
    // method can find the correct user regardless of which store it reads.
    private suspend fun createLocalUser(uid: String, email: String) {
        val user = UserEntity(
            userId = uid,
            name = email,
            age = 0,
            sex = false,
            weight = 0f,
            height = 0,
            minBPM = 0,
            maxBPM = 0,
            avgBPM = 0,
            pentagon = Pentagon(0, 0, 0, 0, 0),
            trainingSlots = emptyList(),
            series = Series(0, 0, 0L)
        )
        userRepository.upsert(user)
        syncStoredUserId(uid)
    }

    private suspend fun syncStoredUserId(uid: String) {
        prefs.saveCurrentUserId(uid)           // read by most UserRepository methods
        userRepository.setCurrentUserId(uid)   // read by refreshSeriesState / currentUserId()
    }

    private fun runOp(block: suspend () -> Unit) {
        loading.value = true
        viewModelScope.launch {
            try {
                block()
                error.value = null
                loggedIn.value = true
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        }
    }
}
