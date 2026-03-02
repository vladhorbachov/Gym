package com.gymshark.domain.repository

import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.domain.models.Nutriments

interface FoodRepository {
    suspend fun getFood(barcode: String): Nutriments?

    suspend fun insertIfNew(
        name: String,
        calories: Float?,
        protein: Float?,
        fat: Float?,
        carbs: Float?
    )

    suspend fun getAllLocal(): List<FoodEntity>
}

