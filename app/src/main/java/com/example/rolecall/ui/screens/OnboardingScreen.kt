package com.example.rolecall.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rolecall.R
import com.example.rolecall.data.local.OnboardingPreferences
import com.example.rolecall.navigation.Routes
import com.example.rolecall.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pages = remember {
        listOf(
            OnboardingPage(
                imageRes = R.drawable.rolecall_logo,
                title = "Welcome to RoleCall",
                description = "Upload your resume and let AI find the best job matches for you."
            ),
            OnboardingPage(
                imageRes = R.drawable.rolecall_logo,
                title = "Smart Matching",
                description = "Our semantic search understands your experience, not just keywords."
            ),
            OnboardingPage(
                imageRes = R.drawable.rolecall_logo,
                title = "Save & Apply",
                description = "Bookmark jobs you love and apply with a single tap."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FoundationDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    scope.launch {
                        OnboardingPreferences.setOnboardingComplete(context)
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Text("Skip", color = SecondaryText)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = pages[page].imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = pages[page].title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = PrimaryText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = pages[page].description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) UiInteractive
                                else SecondaryText.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        scope.launch {
                            OnboardingPreferences.setOnboardingComplete(context)
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UiInteractive)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                    color = PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}