package com.gymshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.gymshark.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun signOut() { auth.signOut() }

    override fun isLoggedIn(): Boolean = auth.currentUser != null
}