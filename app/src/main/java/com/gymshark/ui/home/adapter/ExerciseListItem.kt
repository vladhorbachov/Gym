package com.gymshark.ui.home.adapter

import com.gymshark.data.db.entity.ExercisesEntity

sealed class ExerciseListItem {

    data class Header(
        val title: String
    ) : ExerciseListItem()

    data class ExerciseRow(
        val exercise: ExercisesEntity
    ) : ExerciseListItem()
}