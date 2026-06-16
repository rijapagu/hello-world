package com.dailystrength.domain.workout

import com.dailystrength.domain.model.Difficulty
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.PlannedExercise
import com.dailystrength.domain.model.WorkoutContext
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutSource

/**
 * Deterministic, offline-first workout generator. This is both the primary engine when AI is
 * disabled and the guaranteed fallback when the AI Coach is unavailable or returns an invalid plan.
 *
 * It always produces a valid [WorkoutPlan] (Never Zero), respecting equipment, fitness level, sport
 * adaptation and the 10–20 minute duration bounds.
 */
class WorkoutGenerator {

    /**
     * @param library full exercise library (the only allowed source of exercises).
     */
    fun generate(context: WorkoutContext, library: List<Exercise>): WorkoutPlan {
        val profile = context.profile
        val category = context.suggestedCategory
        val sport = context.sportToday

        val targetDuration = targetDurationMinutes(sport)

        val main = selectExercises(category, profile.usableEquipment, profile.fitnessLevel, library)
        val mobility = if (sport.reducesLowerBody) {
            selectExercises(ExerciseCategory.MOBILITY, profile.usableEquipment, profile.fitnessLevel, library)
                .take(2)
        } else emptyList()

        val chosen = (main + mobility).ifEmpty {
            // Absolute fallback: bodyweight basics guarantee a non-empty plan.
            library.filter { it.isAvailableWith(setOf()) }.take(3)
        }

        val sets = effectiveSets(profile.fitnessLevel, sport)
        var budget = targetDuration * 60
        val planned = mutableListOf<PlannedExercise>()

        for (exercise in chosen) {
            if (budget <= 0 && planned.isNotEmpty()) break
            val reps = if (exercise.isHold) null else calibrateReps(exercise, context)
            val hold = exercise.defaultHoldSeconds
            val rest = if (sport.reducesLowerBody) 30 else 45
            planned += PlannedExercise(
                exerciseId = exercise.id,
                sets = sets,
                targetReps = reps,
                targetHoldSeconds = hold,
                restSeconds = rest,
            )
            budget -= estimateSeconds(sets, reps, hold, rest)
        }

        val duration = clampDuration(targetDuration)
        return WorkoutPlan(
            category = category,
            durationMinutes = duration,
            sportContext = sport,
            source = WorkoutSource.RULE_ENGINE,
            exercises = planned,
        )
    }

    private fun selectExercises(
        category: ExerciseCategory,
        equipment: Set<com.dailystrength.domain.model.Equipment>,
        level: com.dailystrength.domain.model.FitnessLevel,
        library: List<Exercise>,
    ): List<Exercise> {
        val ceiling = level.difficultyCeiling + 1 // allow stretching one level up
        return library
            .filter { it.category == category }
            .filter { it.isAvailableWith(equipment) }
            .filter { it.difficulty.level <= ceiling }
            .sortedBy { difficultyDistance(it.difficulty, level) }
            .take(4)
    }

    private fun difficultyDistance(
        difficulty: Difficulty,
        level: com.dailystrength.domain.model.FitnessLevel,
    ): Int = kotlin.math.abs(difficulty.level - level.difficultyCeiling)

    private fun calibrateReps(exercise: Exercise, context: WorkoutContext): Int {
        val base = exercise.targetRepsFor(context.profile.fitnessLevel)
        val best = context.recentBestReps[exercise.id] ?: return base
        // Progressive overload: aim slightly below recent best to keep volume sustainable.
        return maxOf(base, (best * 0.8).toInt().coerceAtLeast(1))
    }

    private fun effectiveSets(
        level: com.dailystrength.domain.model.FitnessLevel,
        sport: com.dailystrength.domain.model.SportContext,
    ): Int = if (sport.reducesLowerBody) maxOf(2, level.baseSets - 1) else level.baseSets

    private fun targetDurationMinutes(sport: com.dailystrength.domain.model.SportContext): Int =
        if (sport.reducesLowerBody) 12 else 16

    private fun estimateSeconds(sets: Int, reps: Int?, hold: Int?, rest: Int): Int {
        val workPerSet = hold ?: ((reps ?: 8) * 3) // ~3s per rep tempo
        return sets * (workPerSet + rest)
    }

    private fun clampDuration(minutes: Int): Int =
        minutes.coerceIn(WorkoutPlan.MIN_DURATION, WorkoutPlan.MAX_DURATION)
}
