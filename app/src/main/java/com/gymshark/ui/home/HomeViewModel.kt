package com.gymshark.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.user.UserRepository
import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: UserRepository
) : ViewModel() {

    val user: StateFlow<UserEntity?> =
        repo.currentUserIdFlow
            .filterNotNull()
            .flatMapLatest { id -> repo.observeById(id) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveUser(user: UserEntity) {
        viewModelScope.launch {
            repo.upsert(user)
            repo.setCurrentUserId(user.userId)
        }
    }
}
