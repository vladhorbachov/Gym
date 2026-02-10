package com.gymshark.domain.repository

import com.gymshark.data.db.entity.MealInfoEntity

interface MealInfoRepository {

    suspend fun insert(meal: MealInfoEntity)

    suspend fun insertAll(meals: List<MealInfoEntity>)

    suspend fun getAllByDate(date: Long): List<MealInfoEntity>

    suspend fun getByDateAndType(
        date: Long,
        mealType: String
    ): List<MealInfoEntity>

    suspend fun deleteByDate(date: Long)
}