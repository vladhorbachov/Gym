package com.gymshark.data.auth

interface AuthRepository {
    suspend fun register(email: String, password: String)
    suspend fun login(email: String, password: String)
    fun signOut()
    fun isLoggedIn(): Boolean
}