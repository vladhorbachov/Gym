package com.gymshark.ui.home.training.drafts

data class SetEntry(
    val reps: Int? = null,
    val weight: Float? = null
)
fun SetEntry.isPerformed(): Boolean =
    (reps ?: 0) > 0

fun SetEntry.normalizedWeightOrNull(): Float? {
    val w = weight ?: return null
    return if (w > 0f) w else null
}