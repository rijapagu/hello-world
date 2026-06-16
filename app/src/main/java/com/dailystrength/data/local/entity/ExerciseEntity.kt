package com.dailystrength.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dailystrength.domain.model.Difficulty
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.FitnessLevel
import com.dailystrength.domain.model.MuscleGroup

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: ExerciseCategory,
    val difficulty: Difficulty,
    val description: String,
    val instructions: List<String>,
    val commonMistakes: List<String>,
    val requiredEquipment: Set<Equipment>,
    val targetMuscles: Set<MuscleGroup>,
    val videoUrl: String?,
    val animationRef: String?,
    val isUnilateral: Boolean,
    val defaultRepsByLevel: Map<FitnessLevel, Int>,
    val defaultHoldSeconds: Int?,
)
