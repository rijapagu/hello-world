package com.dailystrength.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.usecase.DashboardSnapshot
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(
    autoStart: Boolean = false,
    onOpenWorkout: (Long) -> Unit = {},
    onOpenStats: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(autoStart) {
        if (autoStart) viewModel.onStartClicked()
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.events.flowWithLifecycle(lifecycleOwner.lifecycle).collectLatest { event ->
            when (event) {
                is DashboardEvent.OpenWorkout -> onOpenWorkout(event.workoutId)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        val snapshot = state.snapshot
        when {
            state.loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            snapshot != null -> DashboardContent(
                snapshot = snapshot,
                generating = state.generating,
                onStart = viewModel::onStartClicked,
                onContinue = viewModel::onContinueWorkout,
                onOpenStats = onOpenStats,
            )
        }
    }

    if (state.showSportDialog) {
        SportDialog(onSelect = viewModel::onSportSelected, onDismiss = viewModel::onDismissSportDialog)
    }
}

@Composable
private fun DashboardContent(
    snapshot: DashboardSnapshot,
    generating: Boolean,
    onStart: () -> Unit,
    onContinue: () -> Unit,
    onOpenStats: () -> Unit,
) {
    Text(text = "🔥", fontSize = 40.sp)
    Text(
        text = snapshot.streak.currentStreak.toString(),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 72.sp,
        fontWeight = FontWeight.Black,
    )
    Text(text = "días de racha", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Récord ${snapshot.streak.longestStreak} · ${snapshot.streak.totalWorkouts} workouts · ${snapshot.avatarStage.displayName}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
    )

    Spacer(Modifier.height(28.dp))

    val workout = snapshot.todayWorkout
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Hoy", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = workout?.let { "${categoryLabel(it.category)} · ${it.plannedDurationMin} min" }
                    ?: "Sin workout aún",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            if (workout != null && workout.sportContext != SportContext.NONE) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Adaptado: ${sportLabel(workout.sportContext)}",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    when {
        generating -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        snapshot.isCompletedToday -> Text(
            text = "✓ Completado hoy. Nunca cero.",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        workout != null -> {
            PrimaryButton(text = "Continuar workout", onClick = onContinue)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Regenerar") }
        }
        else -> PrimaryButton(text = "Empezar", onClick = onStart)
    }

    Spacer(Modifier.height(16.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextButton(onClick = onOpenStats) { Text("Ver estadísticas") }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun SportDialog(onSelect: (SportContext) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Jugaste hoy?") },
        text = { Text("Adaptamos el workout si ya jugaste tenis o pádel.") },
        confirmButton = {
            Column {
                DialogChoice("Tenis") { onSelect(SportContext.TENNIS) }
                DialogChoice("Pádel") { onSelect(SportContext.PADEL) }
                DialogChoice("No") { onSelect(SportContext.NONE) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun DialogChoice(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(label, fontSize = 16.sp) }
}

private fun categoryLabel(category: ExerciseCategory): String = when (category) {
    ExerciseCategory.PULL -> "Tirón"
    ExerciseCategory.PUSH -> "Empuje"
    ExerciseCategory.LEGS -> "Piernas"
    ExerciseCategory.CORE -> "Core"
    ExerciseCategory.MOBILITY -> "Movilidad"
    ExerciseCategory.RECOVERY -> "Recuperación"
}

private fun sportLabel(sport: SportContext): String = when (sport) {
    SportContext.TENNIS -> "tenis"
    SportContext.PADEL -> "pádel"
    SportContext.NONE -> "ninguno"
}
