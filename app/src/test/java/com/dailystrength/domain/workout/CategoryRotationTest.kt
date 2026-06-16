package com.dailystrength.domain.workout

import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.SportContext
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CategoryRotationTest {

    @Test
    fun `does not repeat yesterday's category`() {
        val suggestion = CategoryRotation.suggestCategory(
            recentCategories = listOf(ExerciseCategory.PULL),
            sportToday = SportContext.NONE,
        )
        assertNotEquals(ExerciseCategory.PULL, suggestion)
    }

    @Test
    fun `never suggests legs on a sport day`() {
        val suggestion = CategoryRotation.suggestCategory(
            recentCategories = listOf(ExerciseCategory.CORE),
            sportToday = SportContext.PADEL,
        )
        assertNotEquals(ExerciseCategory.LEGS, suggestion)
    }

    @Test
    fun `returns a primary category with empty history`() {
        val suggestion = CategoryRotation.suggestCategory(emptyList(), SportContext.NONE)
        assertTrue(
            suggestion in listOf(
                ExerciseCategory.PULL, ExerciseCategory.PUSH, ExerciseCategory.LEGS,
                ExerciseCategory.CORE, ExerciseCategory.MOBILITY,
            ),
        )
    }
}
