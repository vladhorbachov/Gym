package com.gymshark.data.training.seed

import android.content.Context
import com.gymshark.data.db.entity.ExercisesEntity

interface ExercisesSeedDataSource {
    fun load(context: Context): List<ExercisesEntity>
}
