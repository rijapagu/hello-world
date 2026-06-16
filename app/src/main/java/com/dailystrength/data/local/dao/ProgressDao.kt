package com.dailystrength.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailystrength.data.local.entity.ProgressPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_point WHERE exerciseId = :exerciseId ORDER BY epochDay")
    fun observeForExercise(exerciseId: String): Flow<List<ProgressPointEntity>>

    @Query("SELECT MAX(bestReps) FROM progress_point WHERE exerciseId = :exerciseId")
    suspend fun bestRepsFor(exerciseId: String): Int?

    @Query("SELECT exerciseId, MAX(bestReps) AS best FROM progress_point WHERE bestReps IS NOT NULL GROUP BY exerciseId")
    suspend fun bestRepsAll(): List<ExerciseBestReps>

    @Query("SELECT * FROM progress_point WHERE exerciseId = :exerciseId AND epochDay = :epochDay")
    suspend fun pointFor(exerciseId: String, epochDay: Long): ProgressPointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(point: ProgressPointEntity)
}

/** Projection for the best reps achieved per exercise across all history. */
data class ExerciseBestReps(
    val exerciseId: String,
    val best: Int,
)
