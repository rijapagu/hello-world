package com.dailystrength.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request sent to the AI Coach backend proxy. The proxy holds the LLM credentials and is responsible
 * for prompting the model with the schema; the app only ships structured context. We also send the
 * allowed exercise ids so the backend can constrain/repair the model output server-side.
 */
@Serializable
data class CoachRequestDto(
    @SerialName("system_prompt") val systemPrompt: String,
    @SerialName("profile") val profile: ProfileDto,
    @SerialName("context") val context: ContextDto,
    @SerialName("allowed_exercise_ids") val allowedExerciseIds: List<String>,
)

@Serializable
data class ProfileDto(
    @SerialName("age") val age: Int,
    @SerialName("height_cm") val heightCm: Int,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("fitness_level") val fitnessLevel: String,
    @SerialName("equipment") val equipment: List<String>,
)

@Serializable
data class ContextDto(
    @SerialName("sport_today") val sportToday: String,
    @SerialName("suggested_category") val suggestedCategory: String,
    @SerialName("recent_categories") val recentCategories: List<String>,
    @SerialName("recent_best_reps") val recentBestReps: Map<String, Int>,
    @SerialName("daily_steps") val dailySteps: Int? = null,
)

/**
 * The structured plan returned by the backend. Mirrors the documented AI contract:
 * ```
 * { "workout_type": "pull", "duration": 15,
 *   "exercises": [ { "exercise_id": "assisted_pullup", "sets": 3, "target_reps": 5 } ] }
 * ```
 */
@Serializable
data class WorkoutPlanDto(
    @SerialName("workout_type") val workoutType: String,
    @SerialName("duration") val duration: Int,
    @SerialName("exercises") val exercises: List<PlannedExerciseDto>,
)

@Serializable
data class PlannedExerciseDto(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("sets") val sets: Int,
    @SerialName("target_reps") val targetReps: Int? = null,
    @SerialName("target_hold_seconds") val targetHoldSeconds: Int? = null,
    @SerialName("rest_seconds") val restSeconds: Int = 45,
)
