package com.gymshark.domain.models

import com.gymshark.data.db.entity.ExercisesEntity

data class ExerciseJson (
    val version: Int,
    val list: List<ExercisesEntity>
)