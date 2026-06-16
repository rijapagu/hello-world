package com.dailystrength.data.mapper

import com.dailystrength.data.local.entity.CompletedSetEntity
import com.dailystrength.data.local.entity.ExerciseEntity
import com.dailystrength.data.local.entity.ProgressPointEntity
import com.dailystrength.data.local.entity.StreakEntity
import com.dailystrength.data.local.entity.UserProfileEntity
import com.dailystrength.data.local.entity.WorkoutEntity
import com.dailystrength.data.local.entity.WorkoutExerciseEntity
import com.dailystrength.data.local.entity.WorkoutExerciseWithSets
import com.dailystrength.data.local.entity.WorkoutWithExercises
import com.dailystrength.domain.model.CompletedSet
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ProgressPoint
import com.dailystrength.domain.model.Streak
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.model.WorkoutExercise

// ---- Exercise ----
fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    category = category,
    difficulty = difficulty,
    description = description,
    instructions = instructions,
    commonMistakes = commonMistakes,
    requiredEquipment = requiredEquipment,
    targetMuscles = targetMuscles,
    videoUrl = videoUrl,
    animationRef = animationRef,
    isUnilateral = isUnilateral,
    defaultRepsByLevel = defaultRepsByLevel,
    defaultHoldSeconds = defaultHoldSeconds,
)

fun Exercise.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    category = category,
    difficulty = difficulty,
    description = description,
    instructions = instructions,
    commonMistakes = commonMistakes,
    requiredEquipment = requiredEquipment,
    targetMuscles = targetMuscles,
    videoUrl = videoUrl,
    animationRef = animationRef,
    isUnilateral = isUnilateral,
    defaultRepsByLevel = defaultRepsByLevel,
    defaultHoldSeconds = defaultHoldSeconds,
)

// ---- UserProfile ----
fun UserProfileEntity.toDomain() = UserProfile(
    name = name,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    fitnessLevel = fitnessLevel,
    equipment = equipment,
    googleId = googleId,
    avatarId = avatarId,
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = UserProfileEntity.SINGLETON_ID,
    name = name,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    fitnessLevel = fitnessLevel,
    equipment = equipment,
    googleId = googleId,
    avatarId = avatarId,
)

// ---- Streak ----
fun StreakEntity.toDomain() = Streak(
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastCompletedEpochDay = lastCompletedEpochDay,
    totalWorkouts = totalWorkouts,
)

fun Streak.toEntity() = StreakEntity(
    id = StreakEntity.SINGLETON_ID,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastCompletedEpochDay = lastCompletedEpochDay,
    totalWorkouts = totalWorkouts,
)

// ---- Workout graph ----
fun WorkoutWithExercises.toDomain() = Workout(
    id = workout.id,
    dateEpochDay = workout.dateEpochDay,
    category = workout.category,
    plannedDurationMin = workout.plannedDurationMin,
    sportContext = workout.sportContext,
    source = workout.source,
    status = workout.status,
    completedAtEpochMillis = workout.completedAtEpochMillis,
    exercises = exercises.sortedBy { it.exercise.orderIndex }.map { it.toDomain() },
)

fun WorkoutExerciseWithSets.toDomain() = WorkoutExercise(
    id = exercise.id,
    exerciseId = exercise.exerciseId,
    orderIndex = exercise.orderIndex,
    targetSets = exercise.targetSets,
    targetReps = exercise.targetReps,
    targetHoldSeconds = exercise.targetHoldSeconds,
    restSeconds = exercise.restSeconds,
    completedSets = sets.sortedBy { it.setNumber }.map { it.toDomain() },
)

fun CompletedSetEntity.toDomain() = CompletedSet(
    id = id,
    setNumber = setNumber,
    reps = reps,
    holdSeconds = holdSeconds,
    completedAtEpochMillis = completedAtEpochMillis,
)

// ---- Progress ----
fun ProgressPointEntity.toDomain() = ProgressPoint(
    exerciseId = exerciseId,
    epochDay = epochDay,
    bestReps = bestReps,
    bestHoldSeconds = bestHoldSeconds,
    totalVolume = totalVolume,
)
