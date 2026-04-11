package com.gymshark.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("""
        SELECT * FROM meal_info
        WHERE date BETWEEN :startOfDay AND :endOfDay
        ORDER BY date DESC
    """)
    suspend fun getMealsByDay(
        startOfDay: Long,
        endOfDay: Long
    ): List<MealInfoEntity>

    @Query("""
        SELECT * FROM meal_info
        WHERE date BETWEEN :startOfDay AND :endOfDay
          AND mealType = :mealType
        ORDER BY date DESC
    """)
    suspend fun getMealsByDayAndType(
        startOfDay: Long,
        endOfDay: Long,
        mealType: String
    ): List<MealInfoEntity>

    @Query("""
        SELECT * FROM meal_info
        ORDER BY date DESC
    """)
    suspend fun getAllMeals(): List<MealInfoEntity>

    @Query("""
        DELETE FROM meal_info
        WHERE date BETWEEN :startOfDay AND :endOfDay
    """)
    suspend fun deleteMealsByDay(
        startOfDay: Long,
        endOfDay: Long
    )

    @Query("""
    SELECT * FROM meal_info
    WHERE date < :startOfToday
    ORDER BY date DESC
""")
    suspend fun getHistoryMeals(startOfToday: Long): List<MealInfoEntity>

    @Delete
    suspend fun delete(meal: MealInfoEntity)
}