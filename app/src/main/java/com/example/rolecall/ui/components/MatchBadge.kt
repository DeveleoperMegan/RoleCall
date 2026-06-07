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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A small circular badge that shows the match percentage.
 * Colours: green ≥ 80%, yellow 60‑79%, grey < 60%.
 */
@Composable
fun MatchBadge(score: Float) {
    val badgeColor = when {
        score >= 80f -> Color(0xFF4CAF50)  // green
        score >= 60f -> Color(0xFFFFC107)  // amber/yellow
        else          -> Color(0xFF9E9E9E)  // grey
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(badgeColor)
    ) {
        Text(
            text = "${score.toInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

