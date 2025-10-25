package com.gymshark.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.UserRepository
import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    fun saveUser(user: UserEntity) = viewModelScope.launch {
        userRepository.upsert(user)
        userRepository.setCurrentUserId(user.userId)
    }

    suspend fun loadUser(id: String) = userRepository.getById(id)


}
