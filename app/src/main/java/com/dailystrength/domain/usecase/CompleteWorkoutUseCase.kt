package com.dailystrength.domain.usecase

import com.dailystrength.domain.model.Streak
import com.dailystrength.domain.repository.ProgressRepository
import com.dailystrength.domain.repository.StreakRepository
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.streak.StreakCalculator
import com.dailystrength.domain.util.DateProvider
import javax.inject.Inject

/**
 * Finalizes a workout: marks it completed, advances the streak, and records progression. Returns the
 * updated [Streak] so the UI can celebrate it immediately. The widget is refreshed by the caller
 * (presentation layer) after this completes.
 */
class CompleteWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val streakRepository: StreakRepository,
    private val progressRepository: ProgressRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(workoutId: Long): Streak {
        val now = dateProvider.nowEpochMillis()
        val today = dateProvider.todayEpochDay()

        workoutRepository.markCompleted(workoutId, now)

        val workout = workoutRepository.getWorkout(workoutId)
            ?: error("Cannot complete missing workout $workoutId")
        progressRepository.recordFromWorkout(workout)

        val current = streakRepository.getStreak()
        val updated = StreakCalculator.onWorkoutCompleted(current, today)
        streakRepository.saveStreak(updated)
        return updated
    }
}
