package com.example.rolecall.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rolecall.R
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.screens.AuthViewModel
import com.example.rolecall.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleCallScaffold(
    navController: NavController,
    title: String = "RoleCall",
    showSearchBar: Boolean = false,
    onSearchQueryChanged: (String) -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    fun performSearch() {
        if (searchQuery.isNotBlank()) {
            navController.navigate("${Routes.RESULTS}?query=$searchQuery") {
                popUpTo(Routes.RESULTS) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondaryText)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar with dropdown, logo, and profile ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FoundationDark,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    // Left: Dropdown menu
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = PrimaryText
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Upload Résumé") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Routes.UPLOAD) {
                                    popUpTo(Routes.UPLOAD) { inclusive = true }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Results") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Routes.RESULTS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Routes.HISTORY)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                menuExpanded = false
                                navController.navigate(Routes.PROFILE)
                            }
                        )
                        HorizontalDivider(color = Border)

                        if (authState.isLoggedIn) {
                            DropdownMenuItem(
                                text = { Text("Log Out") },
                                onClick = {
                                    menuExpanded = false
                                    authViewModel.logOut()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Log In") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    // Right: Profile icon
                    IconButton(
                        onClick = {
                            if (authState.isLoggedIn) {
                                navController.navigate(Routes.PROFILE)
                            } else {
                                navController.navigate(Routes.LOGIN)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryText,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FoundationSurface)
                        )
                    }
                }
            }

            // ── Search bar (conditionally shown) ──
            if (showSearchBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = FoundationDark,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchQueryChanged(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Search jobs...", color = SecondaryText)
                        },
                        leadingIcon = {
                            IconButton(onClick = { performSearch() }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = SecondaryText
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                TextButton(onClick = {
                                    searchQuery = ""
                                    onSearchQueryChanged("")
                                }) {
                                    Text("Clear", color = SecondaryText)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UiInteractive,
                            unfocusedBorderColor = Border,
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            cursorColor = UiInteractive
                        )
                    )
                }
            }

            // ── Main content area ──
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                color = FoundationDark,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                content(Modifier.fillMaxSize())
            }
        }

        // ── Floating Logo (hovers above all layers) ──
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 28.dp)
                .zIndex(10f)
                .size(64.dp)
                .clip(CircleShape)
                .background(FoundationDark)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rolecall_logo),
                contentDescription = "RoleCall Logo",
                modifier = Modifier.size(52.dp)
            )
        }
    }
}