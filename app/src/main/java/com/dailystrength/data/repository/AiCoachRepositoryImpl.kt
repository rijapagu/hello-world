package com.dailystrength.data.repository

import com.dailystrength.data.preferences.AppPreferences
import com.dailystrength.data.remote.AiCoachApi
import com.dailystrength.data.remote.dto.ContextDto
import com.dailystrength.data.remote.dto.CoachRequestDto
import com.dailystrength.data.remote.dto.ProfileDto
import com.dailystrength.data.remote.dto.WorkoutPlanDto
import com.dailystrength.data.remote.prompt.PromptBuilder
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.PlannedExercise
import com.dailystrength.domain.model.WorkoutContext
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutSource
import com.dailystrength.domain.repository.AiCoachRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Coach repository. Returns null on ANY failure (disabled flag, network error, parse error, or
 * unmappable output), so [com.dailystrength.domain.usecase.GenerateWorkoutUseCase] can fall back to
 * the deterministic generator. This is what makes the AI a pure enhancer, never a dependency.
 */
@Singleton
class AiCoachRepositoryImpl @Inject constructor(
    private val api: AiCoachApi,
    private val preferences: AppPreferences,
) : AiCoachRepository {

    override suspend fun generatePlan(context: WorkoutContext, library: List<Exercise>): WorkoutPlan? {
        if (!preferences.isAiEnabled()) return null

        val request = buildRequest(context, library)
        val dto = runCatching { api.generatePlan(request) }.getOrNull() ?: return null
        return mapToDomain(dto, context)
    }

    private fun buildRequest(context: WorkoutContext, library: List<Exercise>): CoachRequestDto {
        val p = context.profile
        return CoachRequestDto(
            systemPrompt = PromptBuilder.systemPrompt(),
            profile = ProfileDto(
                age = p.age,
                heightCm = p.heightCm,
                weightKg = p.weightKg,
                fitnessLevel = p.fitnessLevel.name,
                equipment = p.equipment.map { it.name },
            ),
            context = ContextDto(
                sportToday = context.sportToday.name,
                suggestedCategory = context.suggestedCategory.name,
                recentCategories = context.recentCategories.map { it.name },
                recentBestReps = context.recentBestReps,
                dailySteps = context.dailyStepCount,
            ),
            // Only ids the user can actually perform are offered to the model.
            allowedExerciseIds = library
                .filter { it.isAvailableWith(p.usableEquipment) }
                .map { it.id },
        )
    }

    /** Maps the DTO to a domain plan. Returns null if the category is unrecognized. */
    private fun mapToDomain(dto: WorkoutPlanDto, context: WorkoutContext): WorkoutPlan? {
        val category = runCatching {
            ExerciseCategory.valueOf(dto.workoutType.trim().uppercase())
        }.getOrNull() ?: return null

        val exercises = dto.exercises.map {
            PlannedExercise(
                exerciseId = it.exerciseId,
                sets = it.sets,
                targetReps = it.targetReps,
                targetHoldSeconds = it.targetHoldSeconds,
                restSeconds = it.restSeconds,
            )
        }
        if (exercises.isEmpty()) return null

        // WorkoutPlan's init validates duration bounds; guard so a bad value yields null (fallback).
        return runCatching {
            WorkoutPlan(
                category = category,
                durationMinutes = dto.duration,
                sportContext = context.sportToday,
                source = WorkoutSource.AI,
                exercises = exercises,
            )
        }.getOrNull()
    }
}
