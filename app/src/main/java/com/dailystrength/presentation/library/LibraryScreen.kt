package com.dailystrength.presentation.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailystrength.domain.model.Difficulty
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory

@Composable
fun LibraryScreen(
    onOpenExercise: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Ejercicios",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar ejercicio") },
            shape = RoundedCornerShape(16.dp),
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = { viewModel.onCategory(null) },
                label = { Text("Todos") },
            )
            ExerciseCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { viewModel.onCategory(category) },
                    label = { Text(categoryLabel(category)) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.exercises, key = { it.id }) { exercise ->
                ExerciseRow(exercise = exercise, onClick = { onOpenExercise(exercise.id) })
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: Exercise, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = exercise.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${categoryLabel(exercise.category)} · ${difficultyLabel(exercise.difficulty)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = equipmentHint(exercise.requiredEquipment),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
            )
        }
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

private fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> "Fácil"
    Difficulty.MODERATE -> "Medio"
    Difficulty.HARD -> "Difícil"
}

private fun equipmentLabel(equipment: Equipment): String = when (equipment) {
    Equipment.PULL_UP_BAR -> "Barra de dominadas"
    Equipment.DIP_STATION -> "Estación de fondos"
    Equipment.RESISTANCE_BANDS -> "Bandas elásticas"
    Equipment.AB_WHEEL -> "Rueda abdominal"
    Equipment.NONE -> "Peso corporal"
}

private fun equipmentHint(equipment: Set<Equipment>): String {
    val needs = equipment - Equipment.NONE
    return if (needs.isEmpty()) {
        equipmentLabel(Equipment.NONE)
    } else {
        needs.joinToString(", ") { equipmentLabel(it) }
    }
}
