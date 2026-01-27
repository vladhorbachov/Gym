package com.gymshark.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.training.TrainingRepository
import com.gymshark.data.user.UserRepository
import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    fun saveUser(user: UserEntity) = viewModelScope.launch {
        userRepository.upsert(user)
        userRepository.setCurrentUserId(user.userId)
    }


    suspend fun loadUser(id: String) = userRepository.getById(id)

    fun saveBodyWeight(weight: Float) = viewModelScope.launch {
        trainingRepository.saveWeight(weight)
    }

}
