package com.gymshark.ui.home.profile

import com.gymshark.data.db.entity.UserEntity
import com.gymshark.domain.models.DaySlot
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

object ProfilePreviewFormatter {

    fun personalDetails(user: UserEntity?): String {
        if (user == null) return "Not set"
        return buildList {
            add(if (user.sex) "Male" else "Female")
            user.age.takeIf { it > 0 }?.let { add("$it y.o.") }
            user.height.takeIf { it > 0 }?.let { add("$it cm") }
        }.joinToString(" / ").ifBlank { "Not set" }
    }

    fun trainingDays(days: Set<DayOfWeek>, locale: Locale): String {
        if (days.isEmpty()) return "Not selected"
        return weekOrder()
            .filter { it in days }
            .joinToString(", ") { it.getDisplayName(TextStyle.FULL, locale) }
    }

    fun trainingPlan(
        slots: List<DaySlot>,
        selectedDays: Set<DayOfWeek>,
        locale: Locale
    ): String {
        val orderedSlots = slots
            .filter { it.day in selectedDays }
            .sortedBy { it.day.value }
        if (orderedSlots.isEmpty()) return "Not selected"

        return orderedSlots.joinToString(" / ") { slot ->
            val typeText = slot.types
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" + ")
                .ifBlank { "Not set" }
            "${slot.day.getDisplayName(TextStyle.SHORT, locale)}: $typeText"
        }
    }

    fun weight(user: UserEntity?): String =
        user?.weight?.takeIf { it > 0f }?.let { "${it.toInt()} kg" } ?: "Not set"

    fun weekOrder(): List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )
}
