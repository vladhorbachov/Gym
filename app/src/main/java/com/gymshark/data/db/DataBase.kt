package com.gymshark.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymshark.data.db.converter.PentagonConverter
import com.gymshark.data.db.converter.TrainingDayConverter
import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.db.dao.FoodDao
import com.gymshark.data.db.dao.SetsDao
import com.gymshark.data.db.dao.TrainingsDao
import com.gymshark.data.db.dao.UserDao
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.data.db.entity.SetsEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ExercisesEntity::class,
        FoodEntity::class,
        TrainingsEntity::class,
        SetsEntity::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    PentagonConverter::class, TrainingDayConverter::class
)
abstract class DataBase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exercisesDao(): ExercisesDao
    abstract fun foodDao(): FoodDao
    abstract fun trainingsDao(): TrainingsDao
    abstract fun setsDao(): SetsDao
}