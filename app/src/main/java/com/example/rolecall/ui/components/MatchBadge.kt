package com.example.rolecall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rolecall.ui.theme.AccentSuccess
import kotlin.math.abs

/**
 * A small circular badge that shows the match percentage.
 *
 * Uses a four‑color gradient:
 *   Green  → 90–100%
 *   Yellow → 70–89%
 *   Orange → 50–69%
 *   Red    → 0–49%
 *
 * The gradient smoothly transitions based on the score.
 */
@Composable
fun MatchBadge(score: Float) {

    val green = AccentSuccess          // theme green
    val yellow = Color(0xFFFFC107)
    val orange = Color(0xFFFF9800)
    val red = Color(0xFFF44336)

    // Blend between colors based on the score
    val badgeBrush = when {
        score >= 90f -> Brush.linearGradient(listOf(green, yellow))
        score >= 70f -> Brush.linearGradient(listOf(yellow, orange))
        score >= 50f -> Brush.linearGradient(listOf(orange, red))
        else          -> Brush.linearGradient(listOf(red, red))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(badgeBrush)
    ) {
        Text(
            text = "${score.toInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}