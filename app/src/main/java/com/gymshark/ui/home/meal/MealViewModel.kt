package com.gymshark.ui.home.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.domain.models.Nutriments
import com.gymshark.domain.repository.FoodRepository
import com.gymshark.domain.repository.MealInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MealViewModel(
    private val foodRepository: FoodRepository,
    private val mealInfoRepository: MealInfoRepository
) : ViewModel() {
    private val _nutrimentsState = MutableStateFlow<Nutriments?>(null)

    val nutrimentsState: StateFlow<Nutriments?> = _nutrimentsState


    fun getFood(barcode: String) {
        viewModelScope.launch {
            _nutrimentsState.update {
                foodRepository.getFood(barcode)
            }
        }
    }
}