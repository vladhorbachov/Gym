import java.util.Calendar

private fun Int.legacy0to6ToCalendar(): Int = when (this) {
    0 -> Calendar.MONDAY
    1 -> Calendar.TUESDAY
    2 -> Calendar.WEDNESDAY
    3 -> Calendar.THURSDAY
    4 -> Calendar.FRIDAY
    5 -> Calendar.SATURDAY
    6 -> Calendar.SUNDAY
    else -> -1
}

fun List<Int>.normalizeToCalendarListDistinct(): List<Int> {
    val seen = LinkedHashSet<Int>()
    for (v in this) {
        val mapped = when {
            v in 1..7 -> v
            v in 0..6 -> v.legacy0to6ToCalendar()
            else -> -1
        }
        if (mapped in 1..7) seen.add(mapped)
    }
    return seen.toList()
}
