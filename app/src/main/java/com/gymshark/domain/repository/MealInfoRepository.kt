package com.gymshark.domain.repository

import com.gymshark.data.db.entity.MealInfoEntity

interface MealInfoRepository {

    suspend fun insert(meal: MealInfoEntity)

    suspend fun insertAll(meals: List<MealInfoEntity>)

    suspend fun getMealsByDay(
        startOfDay: Long,
        endOfDay: Long
    ): List<MealInfoEntity>

    suspend fun getMealsByDayAndType(
        startOfDay: Long,
        endOfDay: Long,
        mealType: String
    ): List<MealInfoEntity>

    suspend fun deleteMealsByDay(
        startOfDay: Long,
        endOfDay: Long
    )

    suspend fun getTodayMeals(): List<MealInfoEntity>

    suspend fun getAllMeals(): List<MealInfoEntity>

    suspend fun getHistoryMeals(): List<MealInfoEntity>

    suspend fun delete(meal: MealInfoEntity)
}