package com.dailystrength.presentation.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@Composable
fun AvatarScreen(viewModel: AvatarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Tu avatar", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(
            "Evoluciona con tu constancia, no con tu peso.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(20.dp))
        AvatarPortrait(renderUrl = state.renderUrl)

        Spacer(Modifier.height(16.dp))
        Text(state.stageName, color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Black)

        state.nextMilestoneDays?.let { next ->
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { state.progressToNext },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Próximo nivel a los $next días",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
        MilestoneTimeline(state)

        Spacer(Modifier.height(24.dp))
        ConnectAvatarCard(
            connected = state.avatarId != null,
            onConnect = viewModel::onConnectAvatar,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AvatarPortrait(renderUrl: String?) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.8f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (renderUrl != null) {
                AsyncImage(
                    model = renderUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧍", fontSize = 96.sp)
                    Text(
                        "Conecta tu avatar abajo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneTimeline(state: AvatarUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Hitos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            state.milestones.forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    Box(
                        modifier = Modifier.size(12.dp),
                    ) {
                        CircularProgressIndicator(
                            progress = { if (m.unlocked) 1f else 0f },
                            color = if (m.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            strokeWidth = 3.dp,
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "${m.name} · ${m.days} días",
                        color = if (m.isCurrent) MaterialTheme.colorScheme.primary
                        else if (m.unlocked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (m.isCurrent) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectAvatarCard(connected: Boolean, onConnect: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                if (connected) "Avatar conectado ✓" else "Conectar Ready Player Me",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Crea tu avatar en readyplayer.me y pega aquí su id o URL.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Id o URL del avatar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onConnect(input); input = "" },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Conectar")
            }
        }
    }
}
