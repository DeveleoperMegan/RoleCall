package com.example.rolecall.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rolecall.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WebSearchAnimation(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    var phase by remember { mutableIntStateOf(0) }
    val sites = listOf("LinkedIn", "Indeed", "ZipRecruiter", "Monster", "Glassdoor")

    LaunchedEffect(Unit) {
        // Show scanning through sites
        for (i in sites.indices) {
            delay(600)
            phase = i + 1
        }
        delay(1000)
        onFinished()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FoundationDark.copy(alpha = 0.95f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Searching job sites...",
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(24.dp))

        sites.forEachIndexed { index, site ->
            val isActive = phase > index
            Text(
                text = if (isActive) "✓ $site" else site,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) AccentSuccess else SecondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (phase >= sites.size) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Compiling results...",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentSuccess
            )
        }
    }
}