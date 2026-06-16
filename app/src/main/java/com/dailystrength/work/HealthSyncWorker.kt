package com.dailystrength.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dailystrength.domain.health.HealthDataSource
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.usecase.GenerateWorkoutUseCase
import com.dailystrength.domain.util.DateProvider
import com.dailystrength.widget.WidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodically syncs activity data from the health source (Samsung Health when connected). If a
 * tennis/padel session is detected and today's workout hasn't been generated yet, it pre-generates
 * the adapted plan so the widget already reflects it before the user opens the app.
 *
 * With the default [com.dailystrength.data.health.NoopHealthDataSource] this is a no-op
 * ([HealthDataSource.isAvailable] is false), so it is safe to schedule on every device.
 */
@HiltWorker
class HealthSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val healthDataSource: HealthDataSource,
    private val workoutRepository: WorkoutRepository,
    private val generateWorkout: GenerateWorkoutUseCase,
    private val dateProvider: DateProvider,
    private val widgetUpdater: WidgetUpdater,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!healthDataSource.isAvailable()) return Result.success()

        val today = dateProvider.todayEpochDay()
        if (workoutRepository.getWorkoutForDay(today) != null) return Result.success()

        val sport = runCatching { healthDataSource.detectSport(today) }.getOrNull() ?: SportContext.NONE
        // Generate only when sport was detected; otherwise leave the choice to the user's tap.
        if (sport != SportContext.NONE) {
            runCatching { generateWorkout(sportToday = sport, regenerate = false) }
            widgetUpdater.updateAll()
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "health_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<HealthSyncWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
