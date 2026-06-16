package com.dailystrength.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

/**
 * Interactive 3D model viewer backed by SceneView/Filament. Loads a glTF/glb from [modelUrl] (a
 * remote URL or asset path) and renders it with the default orbit camera, so the user can rotate,
 * zoom and pan with touch. Animated models auto-play; bumping [replayKey] reloads to replay from the
 * start. Used for the Ready Player Me avatar and for 3D exercise demonstrations. See ARCHITECTURE.md
 * §10 and §11.
 */
@Composable
fun Model3dView(
    modelUrl: String,
    modifier: Modifier = Modifier,
    replayKey: Int = 0,
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes()

    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(modelUrl, replayKey) {
        loading = true
        failed = false
        val instance = runCatching { modelLoader.loadModelInstance(modelUrl) }.getOrNull()
        childNodes.clear()
        if (instance != null) {
            childNodes.add(ModelNode(modelInstance = instance, scaleToUnits = 1.0f))
        } else {
            failed = true
        }
        loading = false
    }

    Box(modifier) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            childNodes = childNodes,
        )
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
            failed -> Text(
                text = "3D no disponible",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
