package com.gymshark.domain.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class ProductResponse(
    val status: Int,
    val product: Product?
)

@Parcelize
data class Product(
    @SerializedName("product_name")
    val productName: String?,
    val nutriments: Nutriments?
) : Parcelable

@Parcelize
data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Float?,
    @SerializedName("proteins_100g")
    val proteins100g: Float?,
    @SerializedName("fat_100g")
    val fat100g: Float?,
    @SerializedName("carbohydrates_100g")
    val carbohydrates100g: Float?
) : Parcelable
