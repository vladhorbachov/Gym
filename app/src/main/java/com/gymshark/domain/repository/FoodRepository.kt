package com.gymshark.domain.repository

import com.gymshark.domain.models.Nutriments

interface FoodRepository {
    suspend fun getFood(barcode: String): Nutriments?
}

