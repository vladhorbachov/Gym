package com.gymshark.data.exercises

import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.db.entity.ExercisesEntity

class ExerciseRepositoryImpl(val dao: ExercisesDao) : ExerciseRepository {
    override suspend fun insert(exercise: ExercisesEntity) {
        dao.insert(exercise)
    }

    override suspend fun insert(exercise: List<ExercisesEntity>) {
        dao.insert(exercise)
    }

    override suspend fun update(exercise: ExercisesEntity) {
        dao.update(exercise)
    }

    override suspend fun delete(exercise: ExercisesEntity) {
        dao.delete(exercise)
    }

    override suspend fun getById(id: Int): ExercisesEntity? {
        return dao.getById(id)
    }

    override suspend fun getAll(): List<ExercisesEntity> {
        return dao.getAll()
    }

    override suspend fun getByCategory(category: List<String>): List<ExercisesEntity> {
        return dao.getByCategory(category)
    }

    override suspend fun delete() {
        dao.delete()
    }
}