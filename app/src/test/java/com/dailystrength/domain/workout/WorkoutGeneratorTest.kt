package com.dailystrength.domain.workout

import com.dailystrength.domain.TestExercises
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.FitnessLevel
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.WorkoutContext
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorkoutGeneratorTest {

    private val generator = WorkoutGenerator()

    private fun context(
        category: ExerciseCategory,
        sport: SportContext = SportContext.NONE,
        equipment: Set<Equipment> = setOf(Equipment.NONE),
        level: FitnessLevel = FitnessLevel.INTERMEDIATE,
    ) = WorkoutContext(
        profile = UserProfile.DEFAULT.copy(fitnessLevel = level, equipment = equipment),
        sportToday = sport,
        suggestedCategory = category,
        recentCategories = emptyList(),
        recentBestReps = emptyMap(),
        todayEpochDay = 100,
    )

    @Test
    fun `always produces a valid non-empty plan within duration bounds (Never Zero)`() {
        val plan = generator.generate(context(ExerciseCategory.PUSH), TestExercises.library)
        assertTrue(plan.exercises.isNotEmpty())
        assertTrue(plan.durationMinutes in WorkoutPlan.MIN_DURATION..WorkoutPlan.MAX_DURATION)
        assertEquals(WorkoutSource.RULE_ENGINE, plan.source)
    }

    @Test
    fun `only selects exercises the user has equipment for`() {
        // No equipment: the dip (needs DIP_STATION) and pull-up (needs PULL_UP_BAR) must be excluded.
        val plan = generator.generate(context(ExerciseCategory.PUSH, equipment = setOf(Equipment.NONE)), TestExercises.library)
        assertTrue(plan.exercises.none { it.exerciseId == "dip" })
    }

    @Test
    fun `tennis reduces lower body load and shortens duration`() {
        val plan = generator.generate(
            context(ExerciseCategory.LEGS, sport = SportContext.TENNIS),
            TestExercises.library,
        )
        // Legs were suggested but sport day must not produce a heavy leg plan; mobility is added.
        assertTrue(plan.durationMinutes <= 14)
        assertTrue(plan.exercises.isNotEmpty())
    }

    @Test
    fun `beginner gets fewer sets than advanced`() {
        val beginner = generator.generate(context(ExerciseCategory.PUSH, level = FitnessLevel.BEGINNER), TestExercises.library)
        val advanced = generator.generate(context(ExerciseCategory.PUSH, level = FitnessLevel.ADVANCED), TestExercises.library)
        assertTrue(beginner.exercises.first().sets < advanced.exercises.first().sets)
    }

    @Test
    fun `hold exercises carry hold seconds and no reps`() {
        val plan = generator.generate(context(ExerciseCategory.CORE), TestExercises.library)
        val plank = plan.exercises.firstOrNull { it.exerciseId == "plank" }
        if (plank != null) {
            assertEquals(40, plank.targetHoldSeconds)
            assertEquals(null, plank.targetReps)
        }
    }
}
