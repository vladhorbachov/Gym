package com.gymshark.ui.home.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.data.db.entity.MealInfoEntity
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

    fun saveMeal(
        mealType: String,
        productName: String,
        calories: Float?,
        protein: Float?,
        fat: Float?,
        carbs: Float?
    ) {
        viewModelScope.launch {

            foodRepository.insertIfNew(
                name = productName,
                calories = calories,
                protein = protein,
                fat = fat,
                carbs = carbs
            )

            mealInfoRepository.insert(
                MealInfoEntity(
                    date = System.currentTimeMillis(),
                    mealType = mealType,
                    productName = productName,
                    energyKcal100g = calories,
                    proteins100g = protein,
                    fat100g = fat,
                    carbohydrates100g = carbs
                )
            )
        }
    }

    suspend fun getAllLocalFoods(): List<FoodEntity> {
        return foodRepository.getAllLocal()
    }
}