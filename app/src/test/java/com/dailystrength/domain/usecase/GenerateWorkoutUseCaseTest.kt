package com.dailystrength.domain.usecase

import com.dailystrength.domain.TestExercises
import com.dailystrength.domain.health.HealthDataSource
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.PlannedExercise
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.model.WorkoutContext
import com.dailystrength.domain.model.WorkoutExercise
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutSource
import com.dailystrength.domain.model.WorkoutStatus
import com.dailystrength.domain.repository.AiCoachRepository
import com.dailystrength.domain.repository.ExerciseRepository
import com.dailystrength.domain.repository.UserRepository
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.model.ExerciseCategory as Cat
import com.dailystrength.domain.util.DateProvider
import com.dailystrength.domain.workout.WorkoutGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GenerateWorkoutUseCaseTest {

    private val today = 19_900L

    private val profile = UserProfile.DEFAULT.copy(equipment = setOf(com.dailystrength.domain.model.Equipment.NONE))

    private val date = object : DateProvider {
        override fun todayEpochDay() = today
        override fun nowEpochMillis() = 1L
    }

    private val userRepo = object : UserRepository {
        override fun observeProfile(): Flow<UserProfile?> = flowOf(profile)
        override suspend fun getProfile() = profile
        override suspend fun saveProfile(profile: UserProfile) {}
    }

    private val exerciseRepo = object : ExerciseRepository {
        override fun observeAll(): Flow<List<Exercise>> = flowOf(TestExercises.library)
        override suspend fun getAll() = TestExercises.library
        override suspend fun getById(id: String) = TestExercises.library.firstOrNull { it.id == id }
        override suspend fun getByCategory(category: ExerciseCategory) =
            TestExercises.library.filter { it.category == category }
    }

    /** Records the persisted plan and echoes it back as a Workout. */
    private class FakeWorkoutRepo : WorkoutRepository {
        var lastPlan: WorkoutPlan? = null
        override fun observeWorkoutForDay(epochDay: Long): Flow<Workout?> = flowOf(null)
        override suspend fun getWorkoutForDay(epochDay: Long): Workout? = null
        override suspend fun getWorkout(id: Long): Workout? = lastPlan?.let { plan ->
            Workout(
                id = id,
                dateEpochDay = 0,
                category = plan.category,
                plannedDurationMin = plan.durationMinutes,
                sportContext = plan.sportContext,
                source = plan.source,
                status = WorkoutStatus.PENDING,
                completedAtEpochMillis = null,
                exercises = plan.exercises.mapIndexed { i, p ->
                    WorkoutExercise(i.toLong(), p.exerciseId, i, p.sets, p.targetReps, p.targetHoldSeconds, p.restSeconds, emptyList())
                },
            )
        }
        override suspend fun createWorkout(plan: WorkoutPlan, epochDay: Long): Long {
            lastPlan = plan
            return 1L
        }
        override suspend fun logSet(workoutExerciseId: Long, setNumber: Int, reps: Int?, holdSeconds: Int?) {}
        override suspend fun markCompleted(workoutId: Long, completedAtEpochMillis: Long) {}
        override suspend fun recentCategories(beforeEpochDay: Long, limit: Int) = emptyList<ExerciseCategory>()
        override suspend fun recentBestReps() = emptyMap<String, Int>()
    }

    private fun aiReturning(plan: WorkoutPlan?) = object : AiCoachRepository {
        override suspend fun generatePlan(context: WorkoutContext, library: List<Exercise>): WorkoutPlan? = plan
    }

    private fun health(detected: SportContext?) = object : HealthDataSource {
        override fun isAvailable() = detected != null
        override suspend fun dailySteps(epochDay: Long): Int? = null
        override suspend fun detectSport(epochDay: Long): SportContext? = detected
    }

    private fun useCase(
        ai: AiCoachRepository,
        workoutRepo: WorkoutRepository,
        healthSource: HealthDataSource = health(null),
    ) = GenerateWorkoutUseCase(
        userRepository = userRepo,
        exerciseRepository = exerciseRepo,
        workoutRepository = workoutRepo,
        aiCoachRepository = ai,
        generator = WorkoutGenerator(),
        healthDataSource = healthSource,
        dateProvider = date,
    )

    private val validAiPlan = WorkoutPlan(
        category = Cat.PUSH,
        durationMinutes = 15,
        sportContext = SportContext.NONE,
        source = WorkoutSource.AI,
        exercises = listOf(PlannedExercise("push_up", sets = 3, targetReps = 10, targetHoldSeconds = null)),
    )

    @Test
    fun `uses the AI plan when it is valid`() = runTest {
        val repo = FakeWorkoutRepo()
        val workout = useCase(aiReturning(validAiPlan), repo).invoke(SportContext.NONE, regenerate = true)
        assertEquals(WorkoutSource.AI, workout.source)
        assertEquals("push_up", workout.exercises.first().exerciseId)
    }

    @Test
    fun `falls back to the rule engine when the AI hallucinates an exercise`() = runTest {
        val bad = validAiPlan.copy(exercises = listOf(PlannedExercise("not_a_real_exercise", 3, 10, null)))
        val repo = FakeWorkoutRepo()
        val workout = useCase(aiReturning(bad), repo).invoke(SportContext.NONE, regenerate = true)
        assertEquals(WorkoutSource.RULE_ENGINE, workout.source)
    }

    @Test
    fun `falls back to the rule engine when the AI is unavailable`() = runTest {
        val repo = FakeWorkoutRepo()
        val workout = useCase(aiReturning(null), repo).invoke(SportContext.NONE, regenerate = true)
        assertEquals(WorkoutSource.RULE_ENGINE, workout.source)
    }

    @Test
    fun `health-detected sport overrides an unreported sport`() = runTest {
        val repo = FakeWorkoutRepo()
        val workout = useCase(aiReturning(null), repo, health(SportContext.TENNIS))
            .invoke(SportContext.NONE, regenerate = true)
        assertEquals(SportContext.TENNIS, workout.sportContext)
    }
}
