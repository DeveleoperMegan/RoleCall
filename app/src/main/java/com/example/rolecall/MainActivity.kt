package com.example.rolecall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.rolecall.navigation.RoleCallNavGraph
import com.example.rolecall.ui.theme.RoleCallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoleCallTheme {
                val navController = rememberNavController()
                RoleCallNavGraph(navController = navController)
            }
        }
    }
}