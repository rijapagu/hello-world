package com.dailystrength.domain

import com.dailystrength.domain.model.Difficulty
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.FitnessLevel
import com.dailystrength.domain.model.MuscleGroup

/** Minimal exercise fixtures for pure-domain tests. */
object TestExercises {

    fun rep(
        id: String,
        category: ExerciseCategory,
        difficulty: Difficulty = Difficulty.EASY,
        equipment: Set<Equipment> = setOf(Equipment.NONE),
        reps: Int = 8,
    ) = Exercise(
        id = id,
        name = id,
        category = category,
        difficulty = difficulty,
        description = "",
        instructions = emptyList(),
        commonMistakes = emptyList(),
        requiredEquipment = equipment,
        targetMuscles = setOf(MuscleGroup.FULL_BODY),
        videoUrl = null,
        animationRef = null,
        defaultRepsByLevel = FitnessLevel.entries.associateWith { reps },
    )

    fun hold(
        id: String,
        category: ExerciseCategory,
        seconds: Int = 30,
        equipment: Set<Equipment> = setOf(Equipment.NONE),
    ) = Exercise(
        id = id,
        name = id,
        category = category,
        difficulty = Difficulty.EASY,
        description = "",
        instructions = emptyList(),
        commonMistakes = emptyList(),
        requiredEquipment = equipment,
        targetMuscles = setOf(MuscleGroup.ABS),
        videoUrl = null,
        animationRef = null,
        defaultHoldSeconds = seconds,
    )

    val library: List<Exercise> = listOf(
        rep("push_up", ExerciseCategory.PUSH),
        rep("incline_push_up", ExerciseCategory.PUSH, Difficulty.EASY),
        rep("dip", ExerciseCategory.PUSH, Difficulty.HARD, equipment = setOf(Equipment.DIP_STATION)),
        rep("assisted_pullup", ExerciseCategory.PULL, equipment = setOf(Equipment.PULL_UP_BAR)),
        rep("squat", ExerciseCategory.LEGS),
        rep("reverse_lunge", ExerciseCategory.LEGS),
        rep("knee_raise", ExerciseCategory.CORE),
        hold("plank", ExerciseCategory.CORE, 40),
        hold("shoulder_mobility", ExerciseCategory.MOBILITY, 30),
        hold("hip_mobility", ExerciseCategory.MOBILITY, 30),
    )
}
