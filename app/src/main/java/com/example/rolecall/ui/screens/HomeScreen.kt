package com.example.rolecall.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rolecall.R
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    fun submitSearch() {
        if (searchQuery.isNotBlank()) {
            navController.navigate("web_search_animation/$searchQuery")
        }
    }

    RoleCallScaffold(
        navController = navController,
        title = "RoleCall",
        showSearchBar = false
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Prominent logo
            Image(
                painter = painterResource(id = R.drawable.rolecall_logo),
                contentDescription = "RoleCall Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Find Your Next Role",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Search job postings without uploading a resume.",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Android Developer", color = SecondaryText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UiInteractive,
                    unfocusedBorderColor = Border,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = UiInteractive
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { submitSearch() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
            ) {
                Text("Search Jobs", color = PrimaryText)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Already have a resume?",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
            TextButton(onClick = { navController.navigate(Routes.UPLOAD) }) {
                Text("Upload & Match", color = UiInteractive)
            }
        }
    }
}
