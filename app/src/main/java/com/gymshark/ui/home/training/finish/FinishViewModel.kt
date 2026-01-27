package com.gymshark.ui.home.training.finish

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FinishViewModel : ViewModel() {

    private val _mood = MutableStateFlow<Mood>(Mood.NEUTRAL)
    val mood = _mood.asStateFlow()

    fun selectMood(value: Mood) {
        _mood.value = value
    }
}

enum class Mood {
    BAD, NEUTRAL, GOOD, AMAZING
}
