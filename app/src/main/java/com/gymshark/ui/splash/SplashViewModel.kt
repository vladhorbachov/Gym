package com.gymshark.ui.splash

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.exercises.ExerciseRepository
import com.gymshark.data.settings.SettingsRepository
import com.gymshark.domain.models.ExerciseJson
import kotlinx.coroutines.launch

class SplashViewModel(
    private val exRepository: ExerciseRepository,
    private val settRepository: SettingsRepository
) : ViewModel() {


    private val _saveExercise = MutableLiveData<Boolean>()
    val saveExercise: LiveData<Boolean>
        get() = _saveExercise


    fun saveAllExercise(exerciseJson: ExerciseJson) {
        Log.e("TAG", "saveAllExercise: ${settRepository.getVersion()}", )
        if (settRepository.getVersion() < exerciseJson.version) {
            viewModelScope.launch {
                runCatching {
                    exRepository.delete()
                }.onSuccess {
                    runCatching {
                        exRepository.insert(exerciseJson.list)
                    }.onSuccess {
                        _saveExercise.value = true
                        settRepository.setVersion(exerciseJson.version)
                        Log.e("TAG", "saveAllExercise: ${settRepository.getVersion()}", )
                    }
                }
            }
        }else{
            _saveExercise.value = true
            Log.e("TAG", "saveAllExercise: ${settRepository.getVersion()}", )
        }

    }

}