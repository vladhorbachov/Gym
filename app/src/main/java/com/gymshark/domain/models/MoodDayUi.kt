package com.gymshark.domain.models

enum class MoodUi { BAD, NEUTRAL, GOOD, AMAZING }

data class MoodDayUi(
    val day: String,
    val mood: MoodUi
)
fun String.toMoodUi(): MoodUi =
    when (lowercase()) {
        "bad" -> MoodUi.BAD
        "good" -> MoodUi.GOOD
        "amazing" -> MoodUi.AMAZING
        else -> MoodUi.NEUTRAL
    }
