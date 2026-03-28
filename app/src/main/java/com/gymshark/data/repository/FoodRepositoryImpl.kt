package com.gymshark.data.repository

import com.gymshark.data.db.dao.FoodDao
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.data.network.OpenFoodFactsApi
import com.gymshark.domain.models.Nutriments
import com.gymshark.domain.models.Product
import com.gymshark.domain.repository.FoodRepository

class FoodRepositoryImpl(
    private val api: OpenFoodFactsApi,
    private val foodDao: FoodDao
) : FoodRepository {

    override suspend fun getFood(barcode: String): Product? {
        val response = api.getProduct(barcode)
        return if (response.status == 1) response.product else null
    }

    override suspend fun insertIfNew(
        name: String,
        calories: Float?,
        protein: Float?,
        fat: Float?,
        carbs: Float?
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        val existing = foodDao.getByName(cleanName)

        if (existing == null) {
            foodDao.insert(
                FoodEntity(
                    name = cleanName,
                    calories = calories?.toInt() ?: 0,
                    proteins = protein ?: 0f,
                    fats = fat ?: 0f,
                    carbs = carbs ?: 0f,
                    score = 0
                )
            )
        }
    }

    override suspend fun getAllLocal(): List<FoodEntity> {
        return foodDao.getAll()
    }

}