package com.dailystrength.domain.model

/**
 * All inputs the workout generator (AI or rule engine) needs to produce a plan. Pure data; assembled
 * by [com.dailystrength.domain.usecase.GenerateWorkoutUseCase].
 *
 * @param suggestedCategory category chosen by rotation logic (avoids repeating yesterday).
 * @param recentCategories categories trained in the last few days, most-recent first.
 * @param recentBestReps best reps per exerciseId from history, used to calibrate targets.
 */
data class WorkoutContext(
    val profile: UserProfile,
    val sportToday: SportContext,
    val suggestedCategory: ExerciseCategory,
    val recentCategories: List<ExerciseCategory>,
    val recentBestReps: Map<String, Int>,
    val todayEpochDay: Long,
    val dailyStepCount: Int? = null, // from Samsung Health, when available
)
