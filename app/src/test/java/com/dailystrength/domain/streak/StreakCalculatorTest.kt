package com.dailystrength.domain.streak

import com.dailystrength.domain.model.Streak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StreakCalculatorTest {

    @Test
    fun `first ever workout starts streak at one`() {
        val result = StreakCalculator.onWorkoutCompleted(Streak.EMPTY, todayEpochDay = 100)
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
        assertEquals(100, result.lastCompletedEpochDay)
        assertEquals(1, result.totalWorkouts)
    }

    @Test
    fun `consecutive day increments streak`() {
        val day1 = StreakCalculator.onWorkoutCompleted(Streak.EMPTY, 100)
        val day2 = StreakCalculator.onWorkoutCompleted(day1, 101)
        assertEquals(2, day2.currentStreak)
        assertEquals(2, day2.longestStreak)
    }

    @Test
    fun `second workout same day does not change streak or total`() {
        val first = StreakCalculator.onWorkoutCompleted(Streak.EMPTY, 100)
        val again = StreakCalculator.onWorkoutCompleted(first, 100)
        assertEquals(1, again.currentStreak)
        assertEquals(1, again.totalWorkouts)
    }

    @Test
    fun `gap larger than grace resets streak to one`() {
        val base = Streak(currentStreak = 3, longestStreak = 5, lastCompletedEpochDay = 100, totalWorkouts = 9)
        val result = StreakCalculator.onWorkoutCompleted(base, todayEpochDay = 110)
        assertEquals(1, result.currentStreak)
        assertEquals(5, result.longestStreak) // longest preserved
    }

    @Test
    fun `single missed day is rescued when streak earned grace`() {
        val base = Streak(currentStreak = 7, longestStreak = 7, lastCompletedEpochDay = 100, totalWorkouts = 7)
        // Completes 2 days later (one missed day) -> rescued, continues.
        val result = StreakCalculator.onWorkoutCompleted(base, todayEpochDay = 102)
        assertEquals(8, result.currentStreak)
    }

    @Test
    fun `single missed day breaks short streak without grace`() {
        val base = Streak(currentStreak = 3, longestStreak = 3, lastCompletedEpochDay = 100, totalWorkouts = 3)
        val result = StreakCalculator.onWorkoutCompleted(base, todayEpochDay = 102)
        assertEquals(1, result.currentStreak)
    }

    @Test
    fun `reconcile collapses current streak after two missed days`() {
        val base = Streak(currentStreak = 3, longestStreak = 3, lastCompletedEpochDay = 100, totalWorkouts = 3)
        val reconciled = StreakCalculator.reconcile(base, todayEpochDay = 103)
        assertEquals(0, reconciled.currentStreak)
        assertEquals(3, reconciled.longestStreak)
    }

    @Test
    fun `reconcile keeps streak when completed today`() {
        val base = Streak(currentStreak = 4, longestStreak = 4, lastCompletedEpochDay = 100, totalWorkouts = 4)
        val reconciled = StreakCalculator.reconcile(base, todayEpochDay = 100)
        assertEquals(4, reconciled.currentStreak)
    }
}
