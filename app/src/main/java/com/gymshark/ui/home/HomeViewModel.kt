package com.gymshark.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.auth.UserRepository
import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class HomeViewModel(private val repo: UserRepository) : ViewModel() {
    private val _user = MutableSharedFlow<UserEntity?>(replay = 0)
    val user = _user.asSharedFlow()

    fun saveUser(user: UserEntity) {
        viewModelScope.launch {
            repo.save(user)
            _user.emit(repo.getById(user.userId))
        }
    }
}
