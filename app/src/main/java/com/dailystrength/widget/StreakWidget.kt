package com.dailystrength.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dailystrength.domain.model.ExerciseCategory

/**
 * The home-screen streak widget — the app's most important surface. Shows 🔥 streak, today's workout
 * and a one-tap Quick Start that deep-links into the start flow.
 */
class StreakWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadStreakWidgetData(context)
        provideContent { WidgetContent(data) }
    }

    @Composable
    private fun WidgetContent(data: StreakWidgetData) {
        val startIntent = Intent(Intent.ACTION_VIEW, Uri.parse("dailystrength://start"))
            .setPackage("com.dailystrength")

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Background)
                .padding(14.dp)
                .clickable(actionStartActivity(startIntent)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥 ${data.currentStreak}", style = TextStyle(color = Flame, fontSize = 28.sp, fontWeight = FontWeight.Bold))
            }
            Spacer(GlanceModifier.height(6.dp))
            Text(text = subtitle(data), style = TextStyle(color = Muted, fontSize = 13.sp))
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = if (data.isCompletedToday) "Hecho ✓" else "Empezar →",
                style = TextStyle(
                    color = if (data.isCompletedToday) Success else Flame,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }

    private fun subtitle(data: StreakWidgetData): String {
        val category = data.todayCategory?.let(::categoryLabel) ?: "Workout listo"
        val mins = data.durationMinutes?.let { " · $it min" } ?: ""
        return when {
            data.isCompletedToday -> "Completado hoy"
            data.isAtRisk -> "¡No rompas la racha!"
            else -> "$category$mins"
        }
    }

    private fun categoryLabel(category: ExerciseCategory): String = when (category) {
        ExerciseCategory.PULL -> "Tirón"
        ExerciseCategory.PUSH -> "Empuje"
        ExerciseCategory.LEGS -> "Piernas"
        ExerciseCategory.CORE -> "Core"
        ExerciseCategory.MOBILITY -> "Movilidad"
        ExerciseCategory.RECOVERY -> "Recuperación"
    }

    private companion object {
        val Background = ColorProvider(Color(0xFF0B0B0F))
        val Flame = ColorProvider(Color(0xFFFF6A2C))
        val Muted = ColorProvider(Color(0xFF9A9AA6))
        val Success = ColorProvider(Color(0xFF35C77A))
    }
}
