package com.dailystrength.domain.repository

import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.ProgressSeries
import com.dailystrength.domain.model.Streak
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.model.WorkoutContext
import com.dailystrength.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
}

interface ExerciseRepository {
    fun observeAll(): Flow<List<Exercise>>
    suspend fun getAll(): List<Exercise>
    suspend fun getById(id: String): Exercise?
    suspend fun getByCategory(category: ExerciseCategory): List<Exercise>
}

interface WorkoutRepository {
    fun observeWorkoutForDay(epochDay: Long): Flow<Workout?>
    suspend fun getWorkoutForDay(epochDay: Long): Workout?
    suspend fun getWorkout(id: Long): Workout?
    /** Persists a generated plan as a PENDING workout for [epochDay]; returns the new workout id. */
    suspend fun createWorkout(plan: WorkoutPlan, epochDay: Long): Long
    suspend fun logSet(workoutExerciseId: Long, setNumber: Int, reps: Int?, holdSeconds: Int?)
    suspend fun markCompleted(workoutId: Long, completedAtEpochMillis: Long)
    suspend fun recentCategories(beforeEpochDay: Long, limit: Int): List<ExerciseCategory>
    suspend fun recentBestReps(): Map<String, Int>
}

interface StreakRepository {
    fun observeStreak(): Flow<Streak>
    suspend fun getStreak(): Streak
    suspend fun saveStreak(streak: Streak)
}

interface ProgressRepository {
    fun observeSeries(exerciseId: String, label: String): Flow<ProgressSeries>
    /** Aggregates completed sets of a workout into daily progress points. */
    suspend fun recordFromWorkout(workout: Workout)
}

/**
 * Remote AI Coach. Implementations talk to a backend proxy (never embedding LLM keys in the app)
 * and MUST return null on any failure so callers fall back to the deterministic generator.
 */
interface AiCoachRepository {
    suspend fun generatePlan(context: WorkoutContext, library: List<Exercise>): WorkoutPlan?
}
