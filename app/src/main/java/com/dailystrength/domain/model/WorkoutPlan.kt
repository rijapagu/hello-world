package com.dailystrength.domain.model

/**
 * A generated, validated workout plan — the unified output of both the AI Coach and the
 * deterministic rule engine. It is the input to persistence ([Workout]).
 */
data class WorkoutPlan(
    val category: ExerciseCategory,
    val durationMinutes: Int,
    val sportContext: SportContext,
    val source: WorkoutSource,
    val exercises: List<PlannedExercise>,
) {
    init {
        require(durationMinutes in MIN_DURATION..MAX_DURATION) {
            "Workout duration must be between $MIN_DURATION and $MAX_DURATION minutes"
        }
        require(exercises.isNotEmpty()) { "A workout plan must contain at least one exercise (Never Zero)" }
    }

    companion object {
        const val MIN_DURATION = 10
        const val MAX_DURATION = 20
    }
}

/**
 * One prescribed exercise within a [WorkoutPlan]. [exerciseId] must reference an [Exercise] in the
 * library; this is enforced by validation before persistence.
 */
data class PlannedExercise(
    val exerciseId: String,
    val sets: Int,
    val targetReps: Int?,        // null for hold-based exercises
    val targetHoldSeconds: Int?, // null for rep-based exercises
    val restSeconds: Int = 45,
)
