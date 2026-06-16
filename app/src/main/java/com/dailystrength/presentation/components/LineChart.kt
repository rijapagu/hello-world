package com.dailystrength.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Minimal, dependency-free line chart drawn directly on a Compose [Canvas].
 *
 * Renders [points] as a polyline normalized over its own min..max range, with a subtle baseline.
 * Designed for sparkline-style progression strips on the stats screen. No external chart library.
 *
 * Empty and single-point inputs are handled gracefully: an empty list draws only the baseline,
 * and a single point draws a centered horizontal segment.
 */
@Composable
fun LineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        // Subtle baseline along the bottom for visual grounding.
        val baselineY = height
        drawLine(
            color = lineColor.copy(alpha = 0.15f),
            start = Offset(0f, baselineY),
            end = Offset(width, baselineY),
            strokeWidth = 2f,
        )

        if (points.isEmpty()) return@Canvas

        // Vertical inset so the top/bottom of the curve isn't clipped by the stroke.
        val verticalPadding = height * 0.12f
        val usableHeight = (height - verticalPadding * 2f).coerceAtLeast(1f)
        val topY = verticalPadding

        val min = points.min()
        val max = points.max()
        val range = (max - min)

        fun yFor(value: Float): Float {
            // When all values are equal (range == 0) center the line vertically.
            val normalized = if (range == 0f) 0.5f else (value - min) / range
            // Higher value -> higher on screen (smaller y).
            return topY + (1f - normalized) * usableHeight
        }

        if (points.size == 1) {
            val y = yFor(points.first())
            drawLine(
                color = lineColor,
                start = Offset(width * 0.15f, y),
                end = Offset(width * 0.85f, y),
                strokeWidth = 6f,
                cap = StrokeCap.Round,
            )
            return@Canvas
        }

        val stepX = width / (points.size - 1)
        val path = Path()
        points.forEachIndexed { index, value ->
            val x = stepX * index
            val y = yFor(value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round),
        )
    }
}
