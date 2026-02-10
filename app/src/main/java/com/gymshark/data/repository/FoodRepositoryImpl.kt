package com.gymshark.data.repository

import com.gymshark.domain.models.Nutriments
import com.gymshark.data.network.OpenFoodFactsApi
import com.gymshark.domain.repository.FoodRepository

class FoodRepositoryImpl(
    private val api: OpenFoodFactsApi
) : FoodRepository {
    override suspend fun getFood(barcode: String): Nutriments? {
        return api.getProduct(barcode).product?.nutriments
    }

}