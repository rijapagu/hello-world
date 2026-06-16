package com.dailystrength.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.FitnessLevel

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.loading) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Perfil", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(state.name, viewModel::onName, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(state.age, viewModel::onAge, label = { Text("Edad") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(state.heightCm, viewModel::onHeight, label = { Text("Altura (cm)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(state.weightKg, viewModel::onWeight, label = { Text("Peso (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(20.dp))
        Text("Nivel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FitnessLevel.entries.forEach { level ->
                FilterChip(selected = state.fitnessLevel == level, onClick = { viewModel.onLevel(level) }, label = { Text(levelLabel(level)) })
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Equipo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            equipmentOptions.forEach { item ->
                FilterChip(selected = item in state.equipment, onClick = { viewModel.toggleEquipment(item) }, label = { Text(equipmentLabel(item)) })
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Coach con IA", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                Text("Si se desactiva, los workouts usan el motor de reglas.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(checked = state.aiEnabled, onCheckedChange = viewModel::onToggleAi)
        }

        if (state.googleId != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = viewModel::onSignOut) { Text("Cerrar sesión de Google") }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(if (state.saved) "Guardado ✓" else "Guardar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

private val equipmentOptions = listOf(
    Equipment.PULL_UP_BAR, Equipment.DIP_STATION, Equipment.RESISTANCE_BANDS, Equipment.AB_WHEEL,
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
