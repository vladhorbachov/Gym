package com.gymshark.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymshark.domain.models.MealInfo
import com.gymshark.domain.models.Nutriments
import com.gymshark.domain.models.Product

@Entity(tableName = "meal_info")
data class MealInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val date: Long,
    val mealType: String,

    val productName: String?,

    val energyKcal100g: Float?,
    val proteins100g: Float?,
    val fat100g: Float?,
    val carbohydrates100g: Float?
)

fun MealInfo.toEntity(): MealInfoEntity =
    MealInfoEntity(
        date = date,
        mealType = mealType,
        productName = product.productName,
        energyKcal100g = product.nutriments?.energyKcal100g,
        proteins100g = product.nutriments?.proteins100g,
        fat100g = product.nutriments?.fat100g,
        carbohydrates100g = product.nutriments?.carbohydrates100g
    )

fun List<MealInfo>.toEntityList(): List<MealInfoEntity> =
    map { it.toEntity() }

fun MealInfoEntity.toDomain(): MealInfo =
    MealInfo(
        date = date,
        mealType = mealType,
        product = Product(
            productName = productName,
            nutriments = Nutriments(
                energyKcal100g = energyKcal100g,
                proteins100g = proteins100g,
                fat100g = fat100g,
                carbohydrates100g = carbohydrates100g
            )
        )
    )

fun List<MealInfoEntity>.toDomainList(): List<MealInfo> =
    map { it.toDomain() }
