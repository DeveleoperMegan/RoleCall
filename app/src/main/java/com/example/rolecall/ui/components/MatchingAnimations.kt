package com.example.rolecall.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rolecall.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Custom animation that simulates a resume being scanned and matched
 * against job postings. Calls [onFinished] when the animation completes.
 */
@Composable
fun MatchingAnimation(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    var phase by remember { mutableIntStateOf(0) }
    val documentLines = 5
    val jobCards = 3

    // Track which lines have been "matched"
    val matchedLines = remember { mutableStateListOf<Boolean>() }
    LaunchedEffect(Unit) {
        repeat(documentLines) { matchedLines.add(false) }
    }

    // Animation sequence (slowed down for visibility)
    LaunchedEffect(Unit) {
        // Phase 0: scan the document
        for (i in 0 until documentLines) {
            delay(800L)
            matchedLines[i] = true
        }

        delay(600L)
        phase = 1               // job cards appear

        delay(1200L)

        // Mark alternate lines as matches
        for (i in 0 until documentLines) {
            if (i % 2 == 0) {
                matchedLines[i] = true
            }
            delay(300L)
        }

        phase = 2               // "Matching complete!"
        delay(600L)
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FoundationDark.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .border(2.dp, PrimaryText, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0 until documentLines) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .background(
                                            if (matchedLines.getOrElse(i) { false })
                                                AccentSuccess.copy(alpha = 0.7f)
                                            else
                                                PrimaryText.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (matchedLines.getOrElse(i) { false }) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Match",
                                        tint = AccentSuccess,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .scale(1.2f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    if (phase == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .offset(y = (scanPosition * 140).dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            UiInteractive.copy(alpha = 0.6f),
                                            Color.Transparent
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                                    )
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Your Resume", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }

            // Job cards side
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0 until jobCards) {
                    val cardVisible = phase >= 1
                    AnimatedVisibility(
                        visible = cardVisible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(animationSpec = tween(600))
                    ) {
                        JobCardPlaceholder()
                    }
                }
                if (phase == 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Matching complete!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun JobCardPlaceholder() {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(60.dp)
            .background(PrimaryText, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .background(Border.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(6.dp)
                    .background(Border.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(6.dp)
                    .background(Border.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
            )
        }
    }
}