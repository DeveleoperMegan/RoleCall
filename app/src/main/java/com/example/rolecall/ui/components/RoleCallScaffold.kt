package com.example.rolecall.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.rolecall.R
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.screens.AuthViewModel
import com.example.rolecall.ui.theme.*
import androidx.compose.foundation.clickable

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

    var searchQuery by remember { mutableStateOf("") }

    data class BottomNavItem(
        val label: String,
        val route: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val navItems = listOf(
        BottomNavItem("Home", Routes.HOME, Icons.Default.Home),
        BottomNavItem("Upload", Routes.UPLOAD, Icons.Default.Upload),
        BottomNavItem("Search", Routes.SEARCH, Icons.Default.Search),
        BottomNavItem("Applications", Routes.APPLICATIONS, Icons.Default.Work),
        BottomNavItem("Profile", Routes.PROFILE, Icons.Default.Person)
    )
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: Routes.HOME

    fun performSearch() {
        if (searchQuery.isNotBlank()) {
            navController.navigate("web_search_animation/$searchQuery")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondaryText)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with profile icon only (no dropdown)
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

            // Conditional search bar
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
                        placeholder = { Text("Search jobs...", color = SecondaryText) },
                        leadingIcon = {
                            IconButton(onClick = { performSearch() }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = SecondaryText)
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

            // Main content
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                color = FoundationDark,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                content(Modifier.fillMaxSize())
            }

            // Bottom navigation bar
            NavigationBar(
                containerColor = FoundationDark,
                contentColor = PrimaryText
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }

        // Floating logo overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 28.dp)
                .zIndex(10f)
                .size(64.dp)
                .clip(CircleShape)
                .background(FoundationDark)
                .clickable {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rolecall_logo),
                contentDescription = "RoleCall Logo – Go to Home",
                modifier = Modifier.size(52.dp)
            )
        }
    }
}