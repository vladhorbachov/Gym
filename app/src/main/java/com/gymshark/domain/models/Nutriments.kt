package com.gymshark.domain.models

import com.google.gson.annotations.SerializedName


data class ProductResponse(
    val status: Int,
    val product: Product?
)

data class Product(
    @SerializedName("product_name")
    val productName: String?,
    val brands: String?,
    val nutriments: Nutriments?
)

data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Float?,

    @SerializedName("proteins_100g")
    val proteins100g: Float?,

    @SerializedName("fat_100g")
    val fat100g: Float?,

    @SerializedName("carbohydrates_100g")
    val carbohydrates100g: Float?
)
