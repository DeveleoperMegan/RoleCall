package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*

@Composable
fun NotificationSettingsScreen(navController: NavController) {

    var newMatches by remember { mutableStateOf(true) }
    var savedJobUpdates by remember { mutableStateOf(true) }
    var applicationReminders by remember { mutableStateOf(false) }
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }

    RoleCallScaffold(
        navController = navController,
        title = "Notifications",
        showSearchBar = false
    ) { modifier ->
        Column(
            modifier = modifier.padding(24.dp)
        ) {
            Text(
                "Notification Preferences",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FoundationSurface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Job Alerts", style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                    Spacer(modifier = Modifier.height(12.dp))

                    NotificationToggle(
                        label = "New job matches",
                        description = "When new jobs match your résumé",
                        checked = newMatches,
                        onCheckedChange = { newMatches = it }
                    )

                    NotificationToggle(
                        label = "Saved job updates",
                        description = "When a saved job closes or changes",
                        checked = savedJobUpdates,
                        onCheckedChange = { savedJobUpdates = it }
                    )

                    NotificationToggle(
                        label = "Application reminders",
                        description = "Remind me to follow up on applications",
                        checked = applicationReminders,
                        onCheckedChange = { applicationReminders = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FoundationSurface),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Methods", style = MaterialTheme.typography.titleMedium, color = PrimaryText)
                    Spacer(modifier = Modifier.height(12.dp))

                    NotificationToggle(
                        label = "Push notifications",
                        description = "Receive alerts on your device",
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it }
                    )

                    NotificationToggle(
                        label = "Email notifications",
                        description = "Receive alerts via email",
                        checked = emailNotifications,
                        onCheckedChange = { emailNotifications = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Changes are saved automatically",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back to Profile", color = UiInteractive)
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
            Text(description, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = UiInteractive,
                checkedTrackColor = UiInteractive.copy(alpha = 0.3f),
                uncheckedThumbColor = SecondaryText,
                uncheckedTrackColor = Border
            )
        )
    }
}
