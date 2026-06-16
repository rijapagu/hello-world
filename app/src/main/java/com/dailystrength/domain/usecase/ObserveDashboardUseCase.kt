package com.dailystrength.domain.usecase

import com.dailystrength.domain.avatar.AvatarStageCalculator
import com.dailystrength.domain.model.AvatarStage
import com.dailystrength.domain.model.Streak
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.repository.StreakRepository
import com.dailystrength.domain.repository.UserRepository
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.streak.StreakCalculator
import com.dailystrength.domain.util.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Aggregated, reactive dashboard state derived from streak, today's workout and profile. */
data class DashboardSnapshot(
    val streak: Streak,
    val todayWorkout: Workout?,
    val profile: UserProfile?,
    val avatarStage: AvatarStage,
    val avatarProgressToNext: Float,
    val isCompletedToday: Boolean,
    val isStreakAtRisk: Boolean,
)

class ObserveDashboardUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<DashboardSnapshot> {
        val today = dateProvider.todayEpochDay()
        return combine(
            streakRepository.observeStreak(),
            workoutRepository.observeWorkoutForDay(today),
            userRepository.observeProfile(),
        ) { rawStreak, workout, profile ->
            val streak = StreakCalculator.reconcile(rawStreak, today)
            DashboardSnapshot(
                streak = streak,
                todayWorkout = workout,
                profile = profile,
                avatarStage = AvatarStageCalculator.stageFor(streak),
                avatarProgressToNext = AvatarStageCalculator.progressToNext(streak),
                isCompletedToday = streak.isCompletedToday(today),
                isStreakAtRisk = streak.isAtRisk(today),
            )
        }
    }
}
