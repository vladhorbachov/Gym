package com.gymshark.ui.home.training.drafts


data class TrainingDraft(
    val startTime: Long = System.currentTimeMillis(),
    val exercises: List<ExerciseDraft> = emptyList()
)