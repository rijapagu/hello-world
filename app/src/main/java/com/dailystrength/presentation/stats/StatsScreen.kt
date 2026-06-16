package com.dailystrength.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailystrength.presentation.components.LineChart

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Estadísticas",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
            )
        }

        item {
            // Approx training time: ~15 min per completed workout.
            val tiempoAprox = state.totalWorkouts * 15
            StatHeader(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                totalWorkouts = state.totalWorkouts,
                tiempoAproxMin = tiempoAprox,
                trainingStage = state.trainingStage,
            )
        }

        items(state.lifts, key = { it.exerciseId }) { lift ->
            LiftCard(lift)
        }
    }
}

@Composable
private fun StatHeader(
    currentStreak: Int,
    longestStreak: Int,
    totalWorkouts: Int,
    tiempoAproxMin: Int,
    trainingStage: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (trainingStage.isNotBlank()) {
            Text(
                text = trainingStage,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Racha actual", value = currentStreak.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Récord", value = longestStreak.toString(), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Workouts totales", value = totalWorkouts.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Tiempo aprox", value = "$tiempoAproxMin min", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun LiftCard(lift: LiftProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = lift.label,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                val best = lift.latestBestReps
                if (best != null) {
                    Text(
                        text = "$best reps",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            val delta = lift.delta
            if (delta != null) {
                Spacer(Modifier.height(4.dp))
                val sign = if (delta > 0) "+" else ""
                val deltaColor =
                    if (delta >= 0) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.error
                Text(
                    text = "$sign$delta reps desde el inicio",
                    color = deltaColor,
                    fontSize = 13.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            if (lift.points.isEmpty()) {
                Text(
                    text = "Sin datos aún",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            } else {
                LineChart(
                    points = lift.points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    lineColor = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
