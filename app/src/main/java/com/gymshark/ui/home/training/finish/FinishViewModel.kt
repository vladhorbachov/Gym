package com.gymshark.ui.home.training.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinishViewModel(private val user: UserRepository) : ViewModel() {

    private val _mood = MutableStateFlow<Mood>(Mood.NEUTRAL)
    val mood = _mood.asStateFlow()

    fun selectMood(value: Mood) {
        _mood.value = value
    }

    fun registerActivity(currentTime: Long) {
        viewModelScope.launch {
            user.registerActivity(currentTime)
        }
    }

}

enum class Mood {
    BAD, NEUTRAL, GOOD, AMAZING
}
