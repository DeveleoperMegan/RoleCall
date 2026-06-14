package com.example.rolecall.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rolecall.data.model.JobItem
import com.example.rolecall.ui.theme.*

@Composable
fun JobCard(
    job: JobItem,
    onClick: () -> Unit,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {}
) {
    var saved by remember { mutableStateOf(isSaved) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryText
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Border
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.company,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Border
                )
                Text(
                    text = job.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Border.copy(alpha = 0.7f)
                )
            }

            // Save icon
            IconButton(
                onClick = {
                    saved = !saved
                    onSaveClick()
                }
            ) {
                Icon(
                    imageVector = if (saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (saved) "Saved" else "Save",
                    tint = if (saved) AccentSuccess else Border
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MatchBadge(score = job.matchScore)
        }
    }
}