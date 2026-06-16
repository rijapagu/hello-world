package com.dailystrength.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirror of the Android app's CoachRequestDto. */
@Serializable
data class CoachRequest(
    @SerialName("system_prompt") val systemPrompt: String,
    @SerialName("profile") val profile: Profile,
    @SerialName("context") val context: Context,
    @SerialName("allowed_exercise_ids") val allowedExerciseIds: List<String>,
)

@Serializable
data class Profile(
    @SerialName("age") val age: Int,
    @SerialName("height_cm") val heightCm: Int,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("fitness_level") val fitnessLevel: String,
    @SerialName("equipment") val equipment: List<String>,
)

@Serializable
data class Context(
    @SerialName("sport_today") val sportToday: String,
    @SerialName("suggested_category") val suggestedCategory: String,
    @SerialName("recent_categories") val recentCategories: List<String> = emptyList(),
    @SerialName("recent_best_reps") val recentBestReps: Map<String, Int> = emptyMap(),
    @SerialName("daily_steps") val dailySteps: Int? = null,
)

/** Mirror of the Android app's WorkoutPlanDto — the structured contract returned to the app. */
@Serializable
data class WorkoutPlan(
    @SerialName("workout_type") val workoutType: String,
    @SerialName("duration") val duration: Int,
    @SerialName("exercises") val exercises: List<PlannedExercise>,
)

@Serializable
data class PlannedExercise(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("sets") val sets: Int,
    @SerialName("target_reps") val targetReps: Int? = null,
    @SerialName("target_hold_seconds") val targetHoldSeconds: Int? = null,
    @SerialName("rest_seconds") val restSeconds: Int = 45,
)
