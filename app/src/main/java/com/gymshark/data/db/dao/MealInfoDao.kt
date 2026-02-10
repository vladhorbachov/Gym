package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymshark.data.db.entity.MealInfoEntity

@Dao
interface MealInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealInfoEntity>)

    @Query("SELECT * FROM meal_info WHERE date = :date")
    suspend fun getAllByDate(date: Long): List<MealInfoEntity>

    @Query("""
        SELECT * FROM meal_info 
        WHERE date = :date AND mealType = :mealType
    """)
    suspend fun getByDateAndType(
        date: Long,
        mealType: String
    ): List<MealInfoEntity>

    @Query("DELETE FROM meal_info WHERE date = :date")
    suspend fun deleteByDate(date: Long)
}


