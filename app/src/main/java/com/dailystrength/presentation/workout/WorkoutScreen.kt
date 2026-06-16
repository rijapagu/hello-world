package com.dailystrength.presentation.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WorkoutScreen(
    onFinished: (Int) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffectEvents(viewModel, onFinished, lifecycleOwner)

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        when {
            state.loading || state.finishing -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
            state.current == null -> Text(
                "No hay workout para ejecutar.",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onBackground,
            )
            state.resting -> RestView(
                secondsLeft = state.restLeft,
                onSkip = viewModel::skipRest,
                modifier = Modifier.align(Alignment.Center),
            )
            else -> ExerciseView(
                state = state,
                onIncrement = viewModel::increment,
                onDecrement = viewModel::decrement,
                onLog = viewModel::logCurrentSet,
            )
        }
    }
}

@Composable
private fun LaunchedEffectEvents(
    viewModel: WorkoutViewModel,
    onFinished: (Int) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
) {
    androidx.compose.runtime.LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.events.flowWithLifecycle(lifecycleOwner.lifecycle).collectLatest { event ->
            when (event) {
                is WorkoutEvent.Finished -> onFinished(event.currentStreak)
            }
        }
    }
}

@Composable
private fun ExerciseView(
    state: WorkoutUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onLog: () -> Unit,
) {
    val item = state.current ?: return
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.totalExercises.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ejercicio ${state.currentIndex + 1}/${state.totalExercises}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(24.dp))
        Text(
            item.name,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Serie ${state.currentSet} de ${item.targetSets}",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(40.dp))

        if (item.targetHoldSeconds != null) {
            HoldTarget(seconds = item.targetHoldSeconds)
        } else {
            RepCounter(reps = state.repInput, onIncrement = onIncrement, onDecrement = onDecrement, target = item.targetReps)
        }

        Spacer(Modifier.weight(1f))

        if (item.instructions.isNotEmpty()) {
            Text(
                item.instructions.first(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onLog,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text("Registrar serie", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun RepCounter(reps: Int, onIncrement: () -> Unit, onDecrement: () -> Unit, target: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        RoundButton("−", onDecrement)
        Spacer(Modifier.width(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$reps", color = MaterialTheme.colorScheme.primary, fontSize = 72.sp, fontWeight = FontWeight.Black)
            Text("reps" + (target?.let { " · objetivo $it" } ?: ""), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Spacer(Modifier.width(24.dp))
        RoundButton("+", onIncrement)
    }
}

@Composable
private fun RoundButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text(label, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HoldTarget(seconds: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$seconds s", color = MaterialTheme.colorScheme.primary, fontSize = 64.sp, fontWeight = FontWeight.Black)
        Text("mantén la posición", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun RestView(secondsLeft: Int, onSkip: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Descanso", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("$secondsLeft", color = MaterialTheme.colorScheme.primary, fontSize = 88.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onSkip) { Text("Saltar descanso") }
    }
}
