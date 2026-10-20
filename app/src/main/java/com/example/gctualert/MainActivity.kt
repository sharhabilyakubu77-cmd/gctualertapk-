package com.example.gctualert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isLoading by remember { mutableStateOf(true) }

            // Simulate startup loading / session verification
            LaunchedEffect(Unit) {
                delay(1500L)
                isLoading = false
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF007AFF)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loader")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gctu_badge),
                            contentDescription = "GCTU Logo",
                            modifier = Modifier
                                .height(100.dp)
                                .scale(scale)
                        )

                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        )

                        Text(
                            text = "GCTU ALERT",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                } else {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onNavigateToHome = { indexNo ->
                                    navController.navigate("home/$indexNo") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToSignup = { navController.navigate("signup") },
                                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
                            )
                        }
                        composable("signup") {
                            SignupScreen(onNavigateToLogin = { navController.popBackStack() })
                        }
                        composable("forgot_password") {
                            ForgotPasswordScreen(onBack = { navController.popBackStack() })
                        }
                        composable("home/{indexNo}") { backStackEntry ->
                            val indexNo = backStackEntry.arguments?.getString("indexNo") ?: ""
                            HomeScreen(loggedInIndexNumber = indexNo)
                        }
                    }
                }
            }
        }
    }
}
