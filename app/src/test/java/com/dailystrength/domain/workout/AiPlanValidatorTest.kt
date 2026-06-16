package com.dailystrength.domain.workout

import com.dailystrength.domain.TestExercises
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.PlannedExercise
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AiPlanValidatorTest {

    private val profile = UserProfile.DEFAULT.copy(equipment = setOf(Equipment.NONE))

    private fun plan(exercises: List<PlannedExercise>, duration: Int = 15) = WorkoutPlan(
        category = ExerciseCategory.PUSH,
        durationMinutes = duration,
        sportContext = SportContext.NONE,
        source = WorkoutSource.AI,
        exercises = exercises,
    )

    @Test
    fun `valid plan passes`() {
        val result = AiPlanValidator.validate(
            plan(listOf(PlannedExercise("push_up", sets = 3, targetReps = 10, targetHoldSeconds = null))),
            profile,
            TestExercises.library,
        )
        assertEquals(AiPlanValidator.Result.Valid, result)
    }

    @Test
    fun `hallucinated exercise id is rejected`() {
        val result = AiPlanValidator.validate(
            plan(listOf(PlannedExercise("muscle_up_one_arm_planche", sets = 3, targetReps = 5, targetHoldSeconds = null))),
            profile,
            TestExercises.library,
        )
        assertTrue(result is AiPlanValidator.Result.Invalid)
    }

    @Test
    fun `exercise requiring unavailable equipment is rejected`() {
        val result = AiPlanValidator.validate(
            plan(listOf(PlannedExercise("dip", sets = 3, targetReps = 8, targetHoldSeconds = null))),
            profile, // no DIP_STATION
            TestExercises.library,
        )
        assertTrue(result is AiPlanValidator.Result.Invalid)
    }

    @Test
    fun `unreasonable rep target is rejected`() {
        val result = AiPlanValidator.validate(
            plan(listOf(PlannedExercise("push_up", sets = 3, targetReps = 999, targetHoldSeconds = null))),
            profile,
            TestExercises.library,
        )
        assertTrue(result is AiPlanValidator.Result.Invalid)
    }

    @Test
    fun `exercise with neither reps nor hold is rejected`() {
        val result = AiPlanValidator.validate(
            plan(listOf(PlannedExercise("push_up", sets = 3, targetReps = null, targetHoldSeconds = null))),
            profile,
            TestExercises.library,
        )
        assertTrue(result is AiPlanValidator.Result.Invalid)
    }
}
