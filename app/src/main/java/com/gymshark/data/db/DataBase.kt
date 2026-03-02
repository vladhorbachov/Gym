package com.gymshark.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymshark.data.db.converter.DaySlotsConverter
import com.gymshark.data.db.converter.DayTypesConverter
import com.gymshark.data.db.converter.PentagonConverter
import com.gymshark.data.db.converter.TrainingDayConverter
import com.gymshark.data.db.dao.BodyWeightDao
import com.gymshark.data.db.dao.ExercisePrDao
import com.gymshark.data.db.dao.ExercisesDao
import com.gymshark.data.db.dao.FoodDao
import com.gymshark.data.db.dao.MealInfoDao
import com.gymshark.data.db.dao.SetsDao
import com.gymshark.data.db.dao.TrainingsDao
import com.gymshark.data.db.dao.UserDao
import com.gymshark.data.db.entity.BodyWeightEntity
import com.gymshark.data.db.entity.ExercisePrEntity
import com.gymshark.data.db.entity.ExercisesEntity
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.data.db.entity.SetsEntity
import com.gymshark.data.db.entity.TrainingExerciseEntity
import com.gymshark.data.db.entity.TrainingSetEntity
import com.gymshark.data.db.entity.TrainingsEntity
import com.gymshark.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ExercisesEntity::class,
        FoodEntity::class,
        TrainingsEntity::class,
        TrainingExerciseEntity::class,
        TrainingSetEntity::class,
        SetsEntity::class,
        ExercisePrEntity::class,
        BodyWeightEntity::class,
        MealInfoEntity::class
    ],
    version = 9,
    exportSchema = false
)

@TypeConverters(
    PentagonConverter::class,
    TrainingDayConverter::class,
    DayTypesConverter::class,
    DaySlotsConverter::class
)
abstract class DataBase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exercisesDao(): ExercisesDao
    abstract fun foodDao(): FoodDao
    abstract fun trainingsDao(): TrainingsDao
    abstract fun setsDao(): SetsDao
    abstract fun exercisePrDao(): ExercisePrDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun mealInfoDao(): MealInfoDao
}