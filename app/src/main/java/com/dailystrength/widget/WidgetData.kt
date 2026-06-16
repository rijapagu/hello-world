package com.dailystrength.widget

import android.content.Context
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.repository.StreakRepository
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.streak.StreakCalculator
import com.dailystrength.domain.util.DateProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Immutable snapshot the Glance widget renders. */
data class StreakWidgetData(
    val currentStreak: Int,
    val isCompletedToday: Boolean,
    val isAtRisk: Boolean,
    val todayCategory: ExerciseCategory?,
    val durationMinutes: Int?,
) {
    companion object {
        val EMPTY = StreakWidgetData(0, false, false, null, null)
    }
}

/** Hilt entry point so the widget (running outside an @AndroidEntryPoint) can access dependencies. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun streakRepository(): StreakRepository
    fun workoutRepository(): WorkoutRepository
    fun dateProvider(): DateProvider
}

/** Loads the current widget snapshot directly from the database (offline, O(1) reads). */
suspend fun loadStreakWidgetData(context: Context): StreakWidgetData {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetEntryPoint::class.java,
    )
    val today = entryPoint.dateProvider().todayEpochDay()
    val streak = StreakCalculator.reconcile(entryPoint.streakRepository().getStreak(), today)
    val workout = entryPoint.workoutRepository().getWorkoutForDay(today)

    return StreakWidgetData(
        currentStreak = streak.currentStreak,
        isCompletedToday = streak.isCompletedToday(today),
        isAtRisk = streak.isAtRisk(today),
        todayCategory = workout?.category,
        durationMinutes = workout?.plannedDurationMin,
    )
}
