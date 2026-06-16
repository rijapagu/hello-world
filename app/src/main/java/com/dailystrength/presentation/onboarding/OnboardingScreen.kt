package com.dailystrength.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.FitnessLevel
import kotlinx.coroutines.flow.collectLatest

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { if (it is OnboardingEvent.Done) onDone() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Daily Strength", color = MaterialTheme.colorScheme.primary, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Configura tu perfil. Nunca cero.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = { viewModel.onGoogleSignIn(context) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(state.signedInName?.let { "Conectado: $it" } ?: "Continuar con Google (opcional)")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onName,
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.age,
                onValueChange = viewModel::onAge,
                label = { Text("Edad") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.heightCm,
                onValueChange = viewModel::onHeight,
                label = { Text("Altura (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.weightKg,
            onValueChange = viewModel::onWeight,
            label = { Text("Peso (kg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))
        Text("Nivel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FitnessLevel.entries.forEach { level ->
                FilterChip(
                    selected = state.fitnessLevel == level,
                    onClick = { viewModel.onLevel(level) },
                    label = { Text(levelLabel(level)) },
                    colors = FilterChipDefaults.filterChipColors(),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Equipo disponible", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            equipmentOptions.forEach { item ->
                FilterChip(
                    selected = item in state.equipment,
                    onClick = { viewModel.toggleEquipment(item) },
                    label = { Text(equipmentLabel(item)) },
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = viewModel::onFinish,
            enabled = state.canFinish && !state.saving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text("Empezar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

private val equipmentOptions = listOf(
    Equipment.PULL_UP_BAR,
    Equipment.DIP_STATION,
    Equipment.RESISTANCE_BANDS,
    Equipment.AB_WHEEL,
)

private fun levelLabel(level: FitnessLevel): String = when (level) {
    FitnessLevel.BEGINNER -> "Principiante"
    FitnessLevel.INTERMEDIATE -> "Intermedio"
    FitnessLevel.ADVANCED -> "Avanzado"
}

private fun equipmentLabel(item: Equipment): String = when (item) {
    Equipment.PULL_UP_BAR -> "Barra"
    Equipment.DIP_STATION -> "Paralelas"
    Equipment.RESISTANCE_BANDS -> "Bandas"
    Equipment.AB_WHEEL -> "Rueda abd."
    Equipment.NONE -> "Peso corporal"
}
