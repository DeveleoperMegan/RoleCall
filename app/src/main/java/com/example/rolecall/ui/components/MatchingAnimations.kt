package com.example.rolecall.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MatchingAnimation(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var phase by remember { mutableIntStateOf(0) }

    val scannedLines = remember { mutableStateListOf<Boolean>() }
    val matchedLines = remember { mutableStateListOf<Boolean>() }
    val revealedCards = remember { mutableStateListOf<Boolean>() }

    // Lazy list state for scroll animation inside the phone screen
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        repeat(5) { scannedLines.add(false) }
        repeat(5) { matchedLines.add(false) }
        repeat(5) { revealedCards.add(false) }

        // Phase 0: scan document
        for (i in 0 until 5) {
            delay(900L)
            scannedLines[i] = true
        }

        delay(600L)
        phase = 1

        // Phase 1: reveal cards and mark matches
        for (i in 0 until 5) {
            if (i % 2 == 0) {   // lines 0,2,4 are matches
                matchedLines[i] = true
                val cardIndex = i / 2
                if (cardIndex < revealedCards.size) {
                    revealedCards[cardIndex] = true
                    // Scroll to the newly added card (if it's not the first)
                    if (cardIndex > 0) {
                        listState.animateScrollToItem(cardIndex - 1) // scroll so the new card is visible
                    }
                }
            }
            delay(800L)
        }

        // Show all remaining cards (if any not revealed) and scroll to bottom
        for (i in revealedCards.indices) {
            revealedCards[i] = true
        }
        delay(1500L)
        phase = 2
        // Callback to screen
        onComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    val mockJobs = remember {
        listOf(
            JobItem("a", "Android Developer", "TechCorp", "Remote", "", 82f),
            JobItem("b", "Backend Engineer", "StartupXYZ", "New York, NY", "", 74f),
            JobItem("c", "Data Scientist", "DataGenius", "San Francisco, CA", "", 68f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FoundationDark.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── LEFT: Document ────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(180.dp)
                        .border(2.dp, PrimaryText, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (i in 0 until 5) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .background(
                                            if (scannedLines.getOrElse(i) { false })
                                                AccentSuccess.copy(alpha = 0.7f)
                                            else
                                                PrimaryText.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (matchedLines.getOrElse(i) { false }) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Match",
                                        tint = AccentSuccess,
                                        modifier = Modifier.size(16.dp)
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
                                .offset(y = (scanPosition * 155).dp)
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
                Spacer(modifier = Modifier.height(10.dp))
                Text("Your Resume", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }

            // ── RIGHT: Larger phone screen with job cards ───────────────────
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FoundationSurface)
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column {
                    // Mini search bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(FoundationDark.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "  🔍  Search jobs...",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(mockJobs) { index, job ->
                            AnimatedVisibility(
                                visible = revealedCards.getOrElse(index) { false },
                                enter = fadeIn(tween(600)) + slideInVertically(tween(600))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PrimaryText, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            job.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Border,
                                            maxLines = 1
                                        )
                                        Text(
                                            job.company,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Border.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                        Text(
                                            job.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Border.copy(alpha = 0.5f),
                                            maxLines = 1
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(32.dp)
                                            .background(
                                                when {
                                                    job.matchScore >= 80f -> Color(0xFF4CAF50)
                                                    job.matchScore >= 60f -> Color(0xFFFFC107)
                                                    else -> Color(0xFF9E9E9E)
                                                },
                                                RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${job.matchScore.toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── "Matching complete!" banner ────────────────────────────────────
        if (phase == 2) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .background(FoundationDark.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    "Matching complete!",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentSuccess
                )
            }
        }
    }
}