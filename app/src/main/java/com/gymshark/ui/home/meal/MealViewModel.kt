package com.gymshark.ui.home.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.domain.models.Product
import com.gymshark.domain.repository.FoodRepository
import com.gymshark.domain.repository.MealInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealViewModel(
    private val foodRepository: FoodRepository,
    private val mealInfoRepository: MealInfoRepository
) : ViewModel() {

    private val _productState = MutableStateFlow<Product?>(null)
    val productState: StateFlow<Product?> = _productState

    fun getFood(barcode: String) {
        viewModelScope.launch {
            try {
                _productState.value = foodRepository.getFood(barcode)
            } catch (e: Exception) {
                _productState.value = null
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

    fun clearProduct() {
        _productState.value = null
    }
    suspend fun getAllLocalFoods(): List<FoodEntity> {
        return foodRepository.getAllLocal()
    }
}