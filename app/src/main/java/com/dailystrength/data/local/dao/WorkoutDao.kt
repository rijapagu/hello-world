package com.dailystrength.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.dailystrength.data.local.entity.CompletedSetEntity
import com.dailystrength.data.local.entity.WorkoutEntity
import com.dailystrength.data.local.entity.WorkoutExerciseEntity
import com.dailystrength.data.local.entity.WorkoutWithExercises
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.WorkoutStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT * FROM workout WHERE dateEpochDay = :epochDay ORDER BY id DESC LIMIT 1")
    fun observeForDay(epochDay: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout WHERE dateEpochDay = :epochDay ORDER BY id DESC LIMIT 1")
    suspend fun getForDay(epochDay: Long): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun getById(id: Long): WorkoutWithExercises?

    @Query(
        "SELECT category FROM workout WHERE dateEpochDay < :beforeEpochDay " +
            "ORDER BY dateEpochDay DESC LIMIT :limit",
    )
    suspend fun recentCategories(beforeEpochDay: Long, limit: Int): List<ExerciseCategory>

    @Query("UPDATE workout SET status = :status, completedAtEpochMillis = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: WorkoutStatus, completedAt: Long?)

    @Query("SELECT id FROM workout_exercise WHERE id = :workoutExerciseId")
    suspend fun workoutExerciseExists(workoutExerciseId: Long): Long?

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertWorkoutExercises(items: List<WorkoutExerciseEntity>): List<Long>

    @Insert
    suspend fun insertCompletedSet(set: CompletedSetEntity): Long
}
