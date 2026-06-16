package com.dailystrength.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dailystrength.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts the daily "streak at risk" reminder. Tapping it deep-links into the Quick Start flow so the
 * user is one tap from keeping the streak (Never Zero).
 */
@Singleton
class StreakNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Racha",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Recordatorios para mantener tu racha" }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun notifyStreakAtRisk(currentStreak: Int) {
        ensureChannel()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("dailystrength://start"))
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val text = if (currentStreak > 0) {
            "Tu racha de $currentStreak días está en riesgo. 10 minutos bastan."
        } else {
            "Empieza hoy. Nunca cero."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔥 No rompas la racha")
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            // No-ops gracefully if the user hasn't granted POST_NOTIFICATIONS.
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "streak_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
