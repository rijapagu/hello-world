package com.dailystrength.data.local

import androidx.room.TypeConverter
import com.dailystrength.domain.model.Difficulty
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.FitnessLevel
import com.dailystrength.domain.model.MuscleGroup
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.WorkoutSource
import com.dailystrength.domain.model.WorkoutStatus

/**
 * Room type converters. Enums are stored by [Enum.name] (stable & human-readable); collections are
 * stored as a delimited string. Enum names are a stable contract — never rename a constant without
 * a database migration.
 */
class Converters {

    // ---- enums ----
    @TypeConverter fun categoryToString(v: ExerciseCategory) = v.name
    @TypeConverter fun stringToCategory(v: String) = ExerciseCategory.valueOf(v)

    @TypeConverter fun difficultyToString(v: Difficulty) = v.name
    @TypeConverter fun stringToDifficulty(v: String) = Difficulty.valueOf(v)

    @TypeConverter fun levelToString(v: FitnessLevel) = v.name
    @TypeConverter fun stringToLevel(v: String) = FitnessLevel.valueOf(v)

    @TypeConverter fun sportToString(v: SportContext) = v.name
    @TypeConverter fun stringToSport(v: String) = SportContext.valueOf(v)

    @TypeConverter fun sourceToString(v: WorkoutSource) = v.name
    @TypeConverter fun stringToSource(v: String) = WorkoutSource.valueOf(v)

    @TypeConverter fun statusToString(v: WorkoutStatus) = v.name
    @TypeConverter fun stringToStatus(v: String) = WorkoutStatus.valueOf(v)

    // ---- collections ----
    @TypeConverter
    fun stringListToString(v: List<String>): String = v.joinToString(UNIT_SEP)

    @TypeConverter
    fun stringToStringList(v: String): List<String> =
        if (v.isEmpty()) emptyList() else v.split(UNIT_SEP)

    @TypeConverter
    fun equipmentSetToString(v: Set<Equipment>): String = v.joinToString(SEP) { it.name }

    @TypeConverter
    fun stringToEquipmentSet(v: String): Set<Equipment> =
        if (v.isBlank()) emptySet() else v.split(SEP).map { Equipment.valueOf(it) }.toSet()

    @TypeConverter
    fun muscleSetToString(v: Set<MuscleGroup>): String = v.joinToString(SEP) { it.name }

    @TypeConverter
    fun stringToMuscleSet(v: String): Set<MuscleGroup> =
        if (v.isBlank()) emptySet() else v.split(SEP).map { MuscleGroup.valueOf(it) }.toSet()

    @TypeConverter
    fun repMapToString(v: Map<FitnessLevel, Int>): String =
        v.entries.joinToString(SEP) { "${it.key.name}=${it.value}" }

    @TypeConverter
    fun stringToRepMap(v: String): Map<FitnessLevel, Int> =
        if (v.isBlank()) emptyMap() else v.split(SEP).associate {
            val (k, value) = it.split("=")
            FitnessLevel.valueOf(k) to value.toInt()
        }

    private companion object {
        const val SEP = ","
        const val UNIT_SEP = "" // ASCII unit separator: safe inside free-text instructions
    }
}
