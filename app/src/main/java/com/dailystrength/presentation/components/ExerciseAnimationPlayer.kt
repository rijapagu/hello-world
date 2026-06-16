package com.dailystrength.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Renders the demonstration animation for an exercise.
 *
 * Phase 0/1 ships a dependency-free, schematic stick-figure animation that cycles start → movement →
 * end, so every exercise has a working visual without bundling 3D assets. It is intentionally placed
 * behind a stable composable signature so the 3D renderer (SceneView/Filament loading the glTF at
 * [animationRef], with camera rotate/zoom/replay) can be dropped in for Phase 2 without touching the
 * detail screen. See ARCHITECTURE.md §11.
 */
@Composable
fun ExerciseAnimationPlayer(
    animationRef: String?,
    modifier: Modifier = Modifier,
) {
    // `replayKey` restarts the looping transition when the user taps replay.
    var replayKey by remember { mutableIntStateOf(0) }

    // A real glTF/glb asset (URL or .glb path) renders in interactive 3D; otherwise we fall back to
    // the dependency-free schematic so every exercise always has a working demonstration.
    val is3d = animationRef != null &&
        (animationRef.startsWith("http") || animationRef.endsWith(".glb"))

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (is3d) {
            Model3dView(
                modelUrl = animationRef!!,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                replayKey = replayKey,
            )
        } else {
            AnimatedFigure(replayKey = replayKey, modifier = Modifier.fillMaxSize().padding(20.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = { replayKey++ }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Repetir animación",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = if (animationRef != null) "Demostración" else "Animación no disponible",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        )
    }
}

@Composable
private fun AnimatedFigure(replayKey: Int, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "exercise-anim-$replayKey")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phase",
    )

    val lineColor = MaterialTheme.colorScheme.primary
    val jointColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // A simple squat-like flexion drives the schematic: hips and knees lower with `phase`.
        val flex = (sin(phase * Math.PI).toFloat()) // 0..1..0 smooth
        val headR = h * 0.06f
        val hipY = h * (0.45f + 0.12f * flex)
        val shoulderY = hipY - h * 0.18f
        val kneeY = hipY + h * (0.18f - 0.05f * flex)
        val footY = h * 0.9f
        val headY = shoulderY - headR - h * 0.04f

        val stroke = h * 0.018f
        fun seg(a: Offset, b: Offset) =
            drawLine(lineColor, a, b, strokeWidth = stroke, cap = StrokeCap.Round)

        val shoulder = Offset(cx, shoulderY)
        val hip = Offset(cx, hipY)
        // torso
        seg(shoulder, hip)
        // head
        drawCircle(jointColor, radius = headR, center = Offset(cx, headY))
        // arms
        seg(shoulder, Offset(cx - w * 0.16f, shoulderY + h * 0.12f))
        seg(shoulder, Offset(cx + w * 0.16f, shoulderY + h * 0.12f))
        // legs
        val leftKnee = Offset(cx - w * 0.08f, kneeY)
        val rightKnee = Offset(cx + w * 0.08f, kneeY)
        seg(hip, leftKnee); seg(leftKnee, Offset(cx - w * 0.10f, footY))
        seg(hip, rightKnee); seg(rightKnee, Offset(cx + w * 0.10f, footY))
        // ground
        drawLine(
            jointColor.copy(alpha = 0.2f),
            Offset(w * 0.15f, footY),
            Offset(w * 0.85f, footY),
            strokeWidth = stroke * 0.6f,
        )
    }
}
