package com.dailystrength.domain.workout

import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.WorkoutPlan

/**
 * Enforces the hard guarantee that the AI Coach can never break the app: every plan it returns is
 * validated against the local library and the user's constraints before it is allowed to persist.
 * If validation fails, callers fall back to the deterministic [WorkoutGenerator].
 */
object AiPlanValidator {

    sealed interface Result {
        data object Valid : Result
        data class Invalid(val reason: String) : Result
    }

    fun validate(plan: WorkoutPlan, profile: UserProfile, library: List<Exercise>): Result {
        if (plan.exercises.isEmpty()) return Result.Invalid("Empty plan")
        if (plan.durationMinutes !in WorkoutPlan.MIN_DURATION..WorkoutPlan.MAX_DURATION) {
            return Result.Invalid("Duration ${plan.durationMinutes} out of bounds")
        }

        val byId = library.associateBy { it.id }
        for (planned in plan.exercises) {
            val exercise = byId[planned.exerciseId]
                ?: return Result.Invalid("Unknown exercise id '${planned.exerciseId}' (AI hallucination)")

            if (!exercise.isAvailableWith(profile.usableEquipment)) {
                return Result.Invalid("Exercise '${exercise.id}' requires unavailable equipment")
            }
            if (planned.sets !in 1..6) {
                return Result.Invalid("Unreasonable set count ${planned.sets} for '${exercise.id}'")
            }
            val reps = planned.targetReps
            if (reps != null && reps !in 1..50) {
                return Result.Invalid("Unreasonable rep target $reps for '${exercise.id}'")
            }
            val hold = planned.targetHoldSeconds
            if (hold != null && hold !in 5..300) {
                return Result.Invalid("Unreasonable hold $hold for '${exercise.id}'")
            }
            if (reps == null && hold == null) {
                return Result.Invalid("Exercise '${exercise.id}' has neither reps nor hold")
            }
        }
        return Result.Valid
    }
}
