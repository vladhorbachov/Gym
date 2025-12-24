package com.gymshark.ui.home.training.drafts

data class ExerciseDraft(
    val exerciseId: Long,
    val title: String,
    val sets: List<SetEntry> = listOf(SetEntry())
)