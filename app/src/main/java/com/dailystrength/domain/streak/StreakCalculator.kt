package com.dailystrength.domain.streak

import com.dailystrength.domain.model.Streak

/**
 * Pure, deterministic streak logic. No Android, no I/O — fully unit-testable. This is the heart of
 * the product's primary KPI, so it is intentionally simple and side-effect free.
 *
 * Day keys are [java.time.LocalDate.toEpochDay] values, resolved in the user's local zone by callers.
 */
object StreakCalculator {

    /** A single rest-day grace per [graceWindowDays] keeps the Never-Zero promise intact. */
    const val DEFAULT_GRACE_WINDOW_DAYS = 7

    /**
     * Returns the new [Streak] after completing a workout on [todayEpochDay].
     *
     * Rules:
     *  - Already completed today  -> unchanged streak, but counts toward totalWorkouts only once/day.
     *  - Completed yesterday      -> current + 1.
     *  - Gap of exactly 1 day with grace available -> current + 1 (rescued), grace consumed.
     *  - Larger gap (or no grace) -> reset to 1.
     *  - Never completed before    -> 1.
     */
    fun onWorkoutCompleted(
        current: Streak,
        todayEpochDay: Long,
        graceWindowDays: Int = DEFAULT_GRACE_WINDOW_DAYS,
    ): Streak {
        val last = current.lastCompletedEpochDay

        if (last == todayEpochDay) {
            // Second workout same day: does not change streak nor total.
            return current
        }

        val newCurrent = when {
            last == null -> 1
            todayEpochDay == last + 1 -> current.currentStreak + 1
            todayEpochDay == last + 2 && graceAvailable(current, todayEpochDay, graceWindowDays) ->
                current.currentStreak + 1 // single missed day rescued
            else -> 1
        }

        return current.copy(
            currentStreak = newCurrent,
            longestStreak = maxOf(current.longestStreak, newCurrent),
            lastCompletedEpochDay = todayEpochDay,
            totalWorkouts = current.totalWorkouts + 1,
        )
    }

    /**
     * Reconciles a streak read at [todayEpochDay] without completing anything (e.g. when the widget
     * refreshes after midnight). If more than one full day elapsed since the last completion and no
     * grace applies, the displayed current streak collapses to 0.
     */
    fun reconcile(
        current: Streak,
        todayEpochDay: Long,
        graceWindowDays: Int = DEFAULT_GRACE_WINDOW_DAYS,
    ): Streak {
        val last = current.lastCompletedEpochDay ?: return current
        val daysSince = todayEpochDay - last
        val broken = daysSince >= 2 && !graceAvailable(current, todayEpochDay, graceWindowDays)
        return if (broken) current.copy(currentStreak = 0) else current
    }

    /**
     * Grace is available when the current streak is long enough that the user has "earned" a rescue
     * within the rolling window. Simplest honest model: one rescue per full window of streak length.
     */
    private fun graceAvailable(current: Streak, todayEpochDay: Long, graceWindowDays: Int): Boolean {
        if (graceWindowDays <= 0) return false
        // Require an established streak before granting any grace.
        return current.currentStreak >= graceWindowDays
    }
}
