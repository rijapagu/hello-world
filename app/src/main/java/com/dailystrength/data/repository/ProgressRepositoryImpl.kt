package com.dailystrength.data.repository

import com.dailystrength.data.local.dao.ProgressDao
import com.dailystrength.data.local.entity.ProgressPointEntity
import com.dailystrength.data.mapper.toDomain
import com.dailystrength.domain.model.ProgressSeries
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
) : ProgressRepository {

    override fun observeSeries(exerciseId: String, label: String): Flow<ProgressSeries> =
        progressDao.observeForExercise(exerciseId).map { points ->
            ProgressSeries(exerciseId = exerciseId, label = label, points = points.map { it.toDomain() })
        }

    /**
     * Aggregates the completed sets of [workout] into one progress point per (exercise, day),
     * merging with any existing point for idempotency on repeated workouts the same day.
     */
    override suspend fun recordFromWorkout(workout: Workout) {
        val day = workout.dateEpochDay
        for (we in workout.exercises) {
            if (we.completedSets.isEmpty()) continue

            val bestReps = we.completedSets.mapNotNull { it.reps }.maxOrNull()
            val bestHold = we.completedSets.mapNotNull { it.holdSeconds }.maxOrNull()
            val volume = we.completedSets.sumOf { it.reps ?: 0 }

            val existing = progressDao.pointFor(we.exerciseId, day)
            progressDao.upsert(
                ProgressPointEntity(
                    exerciseId = we.exerciseId,
                    epochDay = day,
                    bestReps = maxOfNullable(existing?.bestReps, bestReps),
                    bestHoldSeconds = maxOfNullable(existing?.bestHoldSeconds, bestHold),
                    totalVolume = (existing?.totalVolume ?: 0) + volume,
                ),
            )
        }
    }

    private fun maxOfNullable(a: Int?, b: Int?): Int? = when {
        a == null -> b
        b == null -> a
        else -> maxOf(a, b)
    }
}
