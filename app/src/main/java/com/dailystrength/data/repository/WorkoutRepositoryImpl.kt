package com.dailystrength.data.repository

import androidx.room.withTransaction
import com.dailystrength.data.local.DailyStrengthDatabase
import com.dailystrength.data.local.dao.ProgressDao
import com.dailystrength.data.local.dao.WorkoutDao
import com.dailystrength.data.local.entity.CompletedSetEntity
import com.dailystrength.data.local.entity.WorkoutEntity
import com.dailystrength.data.local.entity.WorkoutExerciseEntity
import com.dailystrength.data.mapper.toDomain
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.model.WorkoutPlan
import com.dailystrength.domain.model.WorkoutStatus
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.util.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val database: DailyStrengthDatabase,
    private val workoutDao: WorkoutDao,
    private val progressDao: ProgressDao,
    private val dateProvider: DateProvider,
) : WorkoutRepository {

    override fun observeWorkoutForDay(epochDay: Long): Flow<Workout?> =
        workoutDao.observeForDay(epochDay).map { it?.toDomain() }

    override suspend fun getWorkoutForDay(epochDay: Long): Workout? =
        workoutDao.getForDay(epochDay)?.toDomain()

    override suspend fun getWorkout(id: Long): Workout? = workoutDao.getById(id)?.toDomain()

    override suspend fun createWorkout(plan: WorkoutPlan, epochDay: Long): Long {
        val workout = WorkoutEntity(
            dateEpochDay = epochDay,
            category = plan.category,
            plannedDurationMin = plan.durationMinutes,
            sportContext = plan.sportContext,
            source = plan.source,
            status = WorkoutStatus.PENDING,
            completedAtEpochMillis = null,
        )
        return database.withTransaction {
            val workoutId = workoutDao.insertWorkout(workout)
            val exercises = plan.exercises.mapIndexed { index, planned ->
                WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseId = planned.exerciseId,
                    orderIndex = index,
                    targetSets = planned.sets,
                    targetReps = planned.targetReps,
                    targetHoldSeconds = planned.targetHoldSeconds,
                    restSeconds = planned.restSeconds,
                )
            }
            workoutDao.insertWorkoutExercises(exercises)
            workoutId
        }
    }

    override suspend fun logSet(workoutExerciseId: Long, setNumber: Int, reps: Int?, holdSeconds: Int?) {
        workoutDao.insertCompletedSet(
            CompletedSetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = setNumber,
                reps = reps,
                holdSeconds = holdSeconds,
                completedAtEpochMillis = dateProvider.nowEpochMillis(),
            ),
        )
    }

    override suspend fun markCompleted(workoutId: Long, completedAtEpochMillis: Long) {
        workoutDao.updateStatus(workoutId, WorkoutStatus.COMPLETED, completedAtEpochMillis)
    }

    override suspend fun recentCategories(beforeEpochDay: Long, limit: Int): List<ExerciseCategory> =
        workoutDao.recentCategories(beforeEpochDay, limit)

    override suspend fun recentBestReps(): Map<String, Int> =
        progressDao.bestRepsAll().associate { it.exerciseId to it.best }
}
