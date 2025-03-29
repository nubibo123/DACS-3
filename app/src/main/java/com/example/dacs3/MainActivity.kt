package com.example.dacs3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.dacs3.data.UserPreferences
import com.example.dacs3.navigation.*
import com.example.dacs3.ui.components.*
import com.example.dacs3.ui.screens.auth.LoginScreen
import com.example.dacs3.ui.screens.auth.SignUpScreen
import com.example.dacs3.ui.screens.profile.ProfileScreen
import com.example.dacs3.ui.theme.DACS3Theme
import com.example.dacs3.ui.viewmodels.*
import com.example.dacs3.ui.screens.chat.ChatScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userPreferences = UserPreferences(this)
        val authViewModel = AuthViewModel(userPreferences)

        setContent {
            DACS3Theme {
                val navController = rememberNavController()
                val authState by authViewModel.authState.collectAsState()
                val authEvent by authViewModel.authEvent.collectAsState()

                Scaffold(
                    bottomBar = {
                        if (authState is AuthState.Success) {
                            BottomBar(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                navController = navController,
                                onLogin = { email, password ->
                                    authViewModel.login(email, password)
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Screen.SignUp.route) {
                            SignUpScreen(
                                navController = navController,
                                onSignUp = { username, email, password ->
                                    authViewModel.signUp(username, email, password)
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(BottomBarScreen.Profile.route) {
                            ProfileScreen(authViewModel = authViewModel)
                        }

                        // Add other screen routes here
                        composable(BottomBarScreen.Chat.route) {
                            // ChatScreen()
                        }

                        composable(BottomBarScreen.Social.route) {
                            // SocialScreen()
                        }

                        composable(BottomBarScreen.VideoCall.route) {
                            // VideoCallScreen()
                        }

                        composable(BottomBarScreen.Notification.route) {
                            // NotificationScreen()
                        }
                    }

                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Success -> {
                                navController.navigate(BottomBarScreen.Profile.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            is AuthState.Error -> {
                                // Error will be handled in the respective screens
                            }
                            else -> {}
                        }
                    }

                    LaunchedEffect(authEvent) {
                        when (authEvent) {
                            is AuthEvent.NavigateToLogin -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                authViewModel.clearEvents()
                            }
                            is AuthEvent.ShowError -> {
                                // Error will be handled in the respective screens
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DACS3Theme {
        Greeting("Android")
    }
}