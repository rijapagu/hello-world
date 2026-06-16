package com.dailystrength.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dailystrength.data.local.DailyStrengthDatabase
import com.dailystrength.data.local.seed.ExerciseSeed
import com.dailystrength.data.mapper.toEntity
import com.dailystrength.data.repository.ProgressRepositoryImpl
import com.dailystrength.data.repository.StreakRepositoryImpl
import com.dailystrength.data.repository.WorkoutRepositoryImpl
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.PlannedExercise
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.Streak
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutSource
import com.dailystrength.domain.model.WorkoutStatus
import com.dailystrength.domain.util.DateProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class RoomRepositoriesTest {

    private lateinit var db: DailyStrengthDatabase
    private lateinit var workoutRepository: WorkoutRepositoryImpl
    private lateinit var streakRepository: StreakRepositoryImpl
    private lateinit var progressRepository: ProgressRepositoryImpl

    private val today = 20_000L
    private val date = object : DateProvider {
        override fun todayEpochDay(): Long = today
        override fun nowEpochMillis(): Long = 1_700_000_000_000L
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, DailyStrengthDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workoutRepository = WorkoutRepositoryImpl(db, db.workoutDao(), db.progressDao(), date)
        streakRepository = StreakRepositoryImpl(db.streakDao())
        progressRepository = ProgressRepositoryImpl(db.progressDao())
    }

    @After
    fun tearDown() = db.close()

    private fun samplePlan() = WorkoutPlan(
        category = ExerciseCategory.PULL,
        durationMinutes = 15,
        sportContext = SportContext.NONE,
        source = WorkoutSource.RULE_ENGINE,
        exercises = listOf(
            PlannedExercise("assisted_pullup", sets = 3, targetReps = 5, targetHoldSeconds = null),
            PlannedExercise("dead_hang", sets = 2, targetReps = null, targetHoldSeconds = 30),
        ),
    )

    @Test
    fun seedLibraryLoadsFiftyExercises() = runTest {
        db.exerciseDao().upsertAll(ExerciseSeed.exercises.map { it.toEntity() })
        assertEquals(50, db.exerciseDao().count())
    }

    @Test
    fun createWorkoutPersistsExercisesInOrder() = runTest {
        val id = workoutRepository.createWorkout(samplePlan(), today)
        val workout = workoutRepository.getWorkout(id)

        assertNotNull(workout)
        assertEquals(ExerciseCategory.PULL, workout!!.category)
        assertEquals(2, workout.exercises.size)
        assertEquals("assisted_pullup", workout.exercises[0].exerciseId)
        assertEquals("dead_hang", workout.exercises[1].exerciseId)
        assertEquals(WorkoutStatus.PENDING, workout.status)
    }

    @Test
    fun getWorkoutForDayReturnsTodaysWorkout() = runTest {
        workoutRepository.createWorkout(samplePlan(), today)
        val workout = workoutRepository.getWorkoutForDay(today)
        assertNotNull(workout)
        assertEquals(today, workout!!.dateEpochDay)
    }

    @Test
    fun loggingSetsAndCompletingProducesProgress() = runTest {
        val id = workoutRepository.createWorkout(samplePlan(), today)
        val workout = workoutRepository.getWorkout(id)!!
        val pullup = workout.exercises.first { it.exerciseId == "assisted_pullup" }

        workoutRepository.logSet(pullup.id, setNumber = 1, reps = 6, holdSeconds = null)
        workoutRepository.logSet(pullup.id, setNumber = 2, reps = 5, holdSeconds = null)
        workoutRepository.markCompleted(id, completedAtEpochMillis = date.nowEpochMillis())

        val completed = workoutRepository.getWorkout(id)!!
        assertEquals(WorkoutStatus.COMPLETED, completed.status)

        progressRepository.recordFromWorkout(completed)
        val best = db.progressDao().bestRepsFor("assisted_pullup")
        assertEquals(6, best)

        val recentBest = workoutRepository.recentBestReps()
        assertEquals(6, recentBest["assisted_pullup"])
    }

    @Test
    fun recentCategoriesReflectsHistory() = runTest {
        workoutRepository.createWorkout(samplePlan(), today - 1)
        val categories = workoutRepository.recentCategories(beforeEpochDay = today, limit = 4)
        assertTrue(categories.contains(ExerciseCategory.PULL))
    }

    @Test
    fun streakRoundTrips() = runTest {
        streakRepository.saveStreak(Streak(currentStreak = 4, longestStreak = 9, lastCompletedEpochDay = today, totalWorkouts = 12))
        val loaded = streakRepository.getStreak()
        assertEquals(4, loaded.currentStreak)
        assertEquals(9, loaded.longestStreak)
        assertEquals(12, loaded.totalWorkouts)
    }
}
