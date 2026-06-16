package com.dailystrength.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dailystrength.domain.repository.StreakRepository
import com.dailystrength.domain.streak.StreakCalculator
import com.dailystrength.domain.util.DateProvider
import com.dailystrength.notification.StreakNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Daily reminder: if the user hasn't completed today's workout, nudge them so the streak survives.
 * Reminding only when not yet done keeps it from being noise. Central to the Never-Zero philosophy.
 */
@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val streakRepository: StreakRepository,
    private val dateProvider: DateProvider,
    private val notifier: StreakNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = dateProvider.todayEpochDay()
        val streak = StreakCalculator.reconcile(streakRepository.getStreak(), today)
        if (!streak.isCompletedToday(today)) {
            notifier.notifyStreakAtRisk(streak.currentStreak)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "streak_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(8, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
