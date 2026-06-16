package com.dailystrength.presentation.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkoutCompleteScreen(streak: Int, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🔥", fontSize = 72.sp)
        Text(
            streak.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 96.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            "días de racha",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = motivational(streak),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text("Hecho", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

private fun motivational(streak: Int): String = when {
    streak <= 1 -> "Empezaste. Eso es todo lo que hace falta hoy."
    streak < 7 -> "Constancia en construcción. Nunca cero."
    streak < 30 -> "Una semana o más. El hábito ya es tuyo."
    streak < 90 -> "Esto ya es identidad, no motivación."
    else -> "Imparable. Sigue apareciendo."
}
