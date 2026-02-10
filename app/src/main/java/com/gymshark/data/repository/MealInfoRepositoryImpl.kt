package com.gymshark.data.repository

import com.gymshark.data.db.dao.MealInfoDao
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.domain.repository.MealInfoRepository

class MealInfoRepositoryImpl(
    val mealInfoDao: MealInfoDao
): MealInfoRepository
{
    override suspend fun insert(meal: MealInfoEntity) {
        mealInfoDao.insert(meal)
    }

    override suspend fun insertAll(meals: List<MealInfoEntity>) {
        mealInfoDao.insertAll(meals)
    }

    override suspend fun getAllByDate(date: Long): List<MealInfoEntity> {
        return mealInfoDao.getAllByDate(date)
    }

    override suspend fun getByDateAndType(
        date: Long,
        mealType: String
    ): List<MealInfoEntity> {
        return mealInfoDao.getByDateAndType(date, mealType)
    }

    override suspend fun deleteByDate(date: Long) {
        mealInfoDao.deleteByDate(date)

    }
}
