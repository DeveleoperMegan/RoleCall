package com.example.rolecall.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rolecall.ui.components.JobCard
import com.example.rolecall.ui.components.RoleCallScaffold
import com.example.rolecall.ui.theme.SecondaryText
import com.google.gson.Gson
import java.net.URLEncoder

@Composable
fun SearchResultsScreen(
    navController: NavController,
    query: String? = null
) {
    val jobs by GenericSearchResultsHolder.results.collectAsState()

    RoleCallScaffold(
        navController = navController,
        title = "Search Results",
        showSearchBar = true
    ) { modifier ->
        if (jobs.isEmpty()) {
            Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No jobs found.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobs) { job ->
                    JobCard(
                        job = job,
                        onClick = {
                            val json = Gson().toJson(job)
                            val encoded = URLEncoder.encode(json, "UTF-8")
                            navController.navigate("job_detail/$encoded")
                        },
                        isSaved = false,
                        onSaveClick = { }
                    )
                }
            }
        }
    }
}