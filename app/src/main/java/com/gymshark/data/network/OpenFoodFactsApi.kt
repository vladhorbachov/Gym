package com.gymshark.data.network

import com.gymshark.domain.models.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {

    @GET("/api/v2/product/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): ProductResponse
}
