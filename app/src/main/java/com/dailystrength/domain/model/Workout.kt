package com.dailystrength.domain.model

/**
 * A persisted workout for a given day. [dateEpochDay] is [java.time.LocalDate.toEpochDay] in the
 * user's local zone — the canonical day key used by the streak engine and the widget.
 */
data class Workout(
    val id: Long,
    val dateEpochDay: Long,
    val category: ExerciseCategory,
    val plannedDurationMin: Int,
    val sportContext: SportContext,
    val source: WorkoutSource,
    val status: WorkoutStatus,
    val completedAtEpochMillis: Long?,
    val exercises: List<WorkoutExercise>,
)

/** A prescribed exercise inside a persisted [Workout], with the actual sets performed. */
data class WorkoutExercise(
    val id: Long,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetHoldSeconds: Int?,
    val restSeconds: Int,
    val completedSets: List<CompletedSet>,
)

/** One actually-performed set, recorded when the user logs it. */
data class CompletedSet(
    val id: Long,
    val setNumber: Int,
    val reps: Int?,
    val holdSeconds: Int?,
    val completedAtEpochMillis: Long,
)
