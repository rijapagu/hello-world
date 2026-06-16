package com.dailystrength.domain.workout

import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.SportContext

/**
 * Deterministic category selection. Rotates through the primary training categories while avoiding
 * yesterday's category, and biases toward mobility/recovery on sport days.
 */
object CategoryRotation {

    private val PRIMARY_CYCLE = listOf(
        ExerciseCategory.PULL,
        ExerciseCategory.PUSH,
        ExerciseCategory.LEGS,
        ExerciseCategory.CORE,
        ExerciseCategory.MOBILITY,
    )

    /**
     * @param recentCategories most-recent-first list of recently trained categories.
     * @param sportToday if the user played tennis/padel, legs are already taxed — prefer upper body
     *        or mobility and never prescribe a heavy leg day.
     */
    fun suggestCategory(
        recentCategories: List<ExerciseCategory>,
        sportToday: SportContext,
    ): ExerciseCategory {
        val yesterday = recentCategories.firstOrNull()

        val candidates = PRIMARY_CYCLE.filter { candidate ->
            candidate != yesterday &&
                !(sportToday.reducesLowerBody && candidate == ExerciseCategory.LEGS)
        }.ifEmpty { PRIMARY_CYCLE.filter { it != ExerciseCategory.LEGS || !sportToday.reducesLowerBody } }

        // Advance the cycle deterministically from the last category.
        val startIndex = yesterday?.let { PRIMARY_CYCLE.indexOf(it) } ?: -1
        val rotated = (1..PRIMARY_CYCLE.size)
            .map { PRIMARY_CYCLE[(startIndex + it).mod(PRIMARY_CYCLE.size)] }
            .firstOrNull { it in candidates }

        return rotated ?: candidates.first()
    }
}
