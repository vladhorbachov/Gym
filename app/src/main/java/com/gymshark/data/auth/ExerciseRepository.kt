package com.gymshark.data.auth

import com.gymshark.data.db.entity.ExercisesEntity

interface ExerciseRepository {
    suspend fun insert(exercise: ExercisesEntity)

    suspend fun insert(exercise: List<ExercisesEntity>)

    suspend fun update(exercise: ExercisesEntity)

    suspend fun delete(exercise: ExercisesEntity)

    suspend fun getById(id: Int): ExercisesEntity?

    suspend fun getAll(): List<ExercisesEntity>

    suspend fun getByCategory(category: String): List<ExercisesEntity>

    suspend fun delete()

}