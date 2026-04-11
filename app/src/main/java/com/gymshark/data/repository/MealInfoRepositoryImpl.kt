package com.gymshark.data.repository

import com.gymshark.data.db.dao.MealInfoDao
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.domain.repository.MealInfoRepository
import java.util.Calendar

class MealInfoRepositoryImpl(
    private val mealInfoDao: MealInfoDao
) : MealInfoRepository {

    override suspend fun insert(meal: MealInfoEntity) {
        mealInfoDao.insert(meal)
    }

    override suspend fun insertAll(meals: List<MealInfoEntity>) {
        mealInfoDao.insertAll(meals)
    }

    override suspend fun getMealsByDay(
        startOfDay: Long,
        endOfDay: Long
    ): List<MealInfoEntity> {
        return mealInfoDao.getMealsByDay(startOfDay, endOfDay)
    }

    override suspend fun getMealsByDayAndType(
        startOfDay: Long,
        endOfDay: Long,
        mealType: String
    ): List<MealInfoEntity> {
        return mealInfoDao.getMealsByDayAndType(startOfDay, endOfDay, mealType)
    }

    override suspend fun deleteMealsByDay(
        startOfDay: Long,
        endOfDay: Long
    ) {
        mealInfoDao.deleteMealsByDay(startOfDay, endOfDay)
    }

    override suspend fun getTodayMeals(): List<MealInfoEntity> {
        return mealInfoDao.getMealsByDay(
            startOfDay = getStartOfDayMillis(),
            endOfDay = getEndOfDayMillis()
        )
    }

    override suspend fun getAllMeals(): List<MealInfoEntity> {
        return mealInfoDao.getAllMeals()
    }

    override suspend fun delete(meal: MealInfoEntity) {
        mealInfoDao.delete(meal)
    }

    private fun getStartOfDayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    override suspend fun getHistoryMeals(): List<MealInfoEntity> {
        return mealInfoDao.getHistoryMeals(getStartOfDayMillis())
    }
}