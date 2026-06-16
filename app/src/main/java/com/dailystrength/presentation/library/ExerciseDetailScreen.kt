package com.dailystrength.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.dailystrength.domain.model.Difficulty
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.model.MuscleGroup
import com.dailystrength.presentation.components.ExerciseAnimationPlayer

@Composable
fun ExerciseDetailScreen(viewModel: ExerciseDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        state.exercise == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Ejercicio no encontrado",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                )
            }
        }

        else -> ExerciseDetailContent(state.exercise!!)
    }
}

@Composable
private fun ExerciseDetailContent(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        Text(
            text = exercise.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${categoryLabel(exercise.category)} · ${difficultyLabel(exercise.difficulty)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(20.dp))

        ExerciseAnimationPlayer(
            animationRef = exercise.animationRef,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )

        Spacer(Modifier.height(24.dp))

        Section(title = "Descripción") {
            Text(
                text = exercise.description,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
            )
        }

        if (exercise.instructions.isNotEmpty()) {
            Section(title = "Instrucciones") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.instructions.forEachIndexed { index, step ->
                        Row {
                            Text(
                                text = "${index + 1}.",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = step,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }

        if (exercise.commonMistakes.isNotEmpty()) {
            Section(title = "Errores comunes") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.commonMistakes.forEach { mistake ->
                        Row {
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(
                                text = mistake,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }

        if (exercise.targetMuscles.isNotEmpty()) {
            Section(title = "Músculos") {
                Text(
                    text = exercise.targetMuscles.joinToString(", ") { muscleLabel(it) },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                )
            }
        }

        Section(title = "Equipo") {
            Text(
                text = equipmentHint(exercise.requiredEquipment),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(8.dp))
    content()
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

private fun muscleLabel(muscle: MuscleGroup): String = when (muscle) {
    MuscleGroup.LATS -> "Dorsales"
    MuscleGroup.BICEPS -> "Bíceps"
    MuscleGroup.FOREARMS -> "Antebrazos"
    MuscleGroup.UPPER_BACK -> "Espalda alta"
    MuscleGroup.REAR_DELTS -> "Deltoides posteriores"
    MuscleGroup.CHEST -> "Pecho"
    MuscleGroup.TRICEPS -> "Tríceps"
    MuscleGroup.FRONT_DELTS -> "Deltoides anteriores"
    MuscleGroup.SHOULDERS -> "Hombros"
    MuscleGroup.QUADS -> "Cuádriceps"
    MuscleGroup.HAMSTRINGS -> "Isquiotibiales"
    MuscleGroup.GLUTES -> "Glúteos"
    MuscleGroup.CALVES -> "Gemelos"
    MuscleGroup.ABS -> "Abdominales"
    MuscleGroup.OBLIQUES -> "Oblicuos"
    MuscleGroup.LOWER_BACK -> "Espalda baja"
    MuscleGroup.HIPS -> "Caderas"
    MuscleGroup.ANKLES -> "Tobillos"
    MuscleGroup.FULL_BODY -> "Cuerpo completo"
}
