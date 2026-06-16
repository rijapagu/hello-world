package com.dailystrength.domain.model

/**
 * Denormalized streak state. Read in O(1) by the widget and dashboard. The single source of truth
 * for the app's primary KPI.
 *
 * @param lastCompletedEpochDay the most recent day a workout was completed, or null if never.
 */
data class Streak(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedEpochDay: Long? = null,
    val totalWorkouts: Int = 0,
) {
    /** True if the user has already completed a workout for [todayEpochDay]. */
    fun isCompletedToday(todayEpochDay: Long): Boolean = lastCompletedEpochDay == todayEpochDay

    /**
     * True when the streak is "at risk": completed yesterday but not today. Drives widget urgency
     * and reminders. Central to the Never-Zero philosophy.
     */
    fun isAtRisk(todayEpochDay: Long): Boolean =
        lastCompletedEpochDay != null &&
            lastCompletedEpochDay == todayEpochDay - 1

    companion object {
        val EMPTY = Streak()
    }
}
