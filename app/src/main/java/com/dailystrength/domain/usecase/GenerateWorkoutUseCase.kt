package com.dailystrength.domain.usecase

import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.model.WorkoutContext
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.health.HealthDataSource
import com.dailystrength.domain.repository.AiCoachRepository
import com.dailystrength.domain.repository.ExerciseRepository
import com.dailystrength.domain.repository.UserRepository
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.util.DateProvider
import com.dailystrength.domain.workout.AiPlanValidator
import com.dailystrength.domain.workout.CategoryRotation
import com.dailystrength.domain.workout.WorkoutGenerator
import javax.inject.Inject

/**
 * Produces today's workout for the given [SportContext] and persists it. Tries the AI Coach first
 * (when enabled & online), validates its output, and falls back to the deterministic generator —
 * guaranteeing a valid workout always exists (Never Zero).
 */
class GenerateWorkoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val aiCoachRepository: AiCoachRepository,
    private val generator: WorkoutGenerator,
    private val healthDataSource: HealthDataSource,
    private val dateProvider: DateProvider,
) {
    /**
     * @param regenerate if false and a workout already exists for today, returns the existing one.
     */
    suspend operator fun invoke(
        sportToday: SportContext,
        regenerate: Boolean = false,
    ): Workout {
        val today = dateProvider.todayEpochDay()

        if (!regenerate) {
            workoutRepository.getWorkoutForDay(today)?.let { return it }
        }

        val profile = userRepository.getProfile() ?: UserProfile.DEFAULT
        val library = exerciseRepository.getAll()

        // Samsung Health can auto-fill the sport the user didn't report, and provide step context.
        val effectiveSport = if (sportToday == SportContext.NONE) {
            runCatching { healthDataSource.detectSport(today) }.getOrNull() ?: SportContext.NONE
        } else {
            sportToday
        }
        val steps = runCatching { healthDataSource.dailySteps(today) }.getOrNull()

        val recent = workoutRepository.recentCategories(beforeEpochDay = today, limit = 4)
        val context = WorkoutContext(
            profile = profile,
            sportToday = effectiveSport,
            suggestedCategory = CategoryRotation.suggestCategory(recent, effectiveSport),
            recentCategories = recent,
            recentBestReps = workoutRepository.recentBestReps(),
            todayEpochDay = today,
            dailyStepCount = steps,
        )

        val plan = resolvePlan(context, profile, library)
        val workoutId = workoutRepository.createWorkout(plan, today)
        return workoutRepository.getWorkout(workoutId)
            ?: error("Workout $workoutId missing immediately after creation")
    }

    private suspend fun resolvePlan(
        context: WorkoutContext,
        profile: UserProfile,
        library: List<com.dailystrength.domain.model.Exercise>,
    ): WorkoutPlan {
        val aiPlan = runCatching { aiCoachRepository.generatePlan(context, library) }.getOrNull()
        if (aiPlan != null && AiPlanValidator.validate(aiPlan, profile, library) is AiPlanValidator.Result.Valid) {
            return aiPlan
        }
        return generator.generate(context, library)
    }
}
