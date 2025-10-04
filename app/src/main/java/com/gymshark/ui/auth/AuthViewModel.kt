package com.gymshark.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    val loading = MutableLiveData(false)
    val error = MutableLiveData<String?>()
    val loggedIn = MutableLiveData(repo.isLoggedIn())

    fun register(email: String, password: String) = run { runOp { repo.register(email, password) } }
    fun login(email: String, password: String) = run { runOp { repo.login(email, password) } }
    fun signOut() {
        repo.signOut(); loggedIn.value = false
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