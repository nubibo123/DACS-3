package com.example.dacs3.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object RandomCall : Screen("random_call")
    object VideoCall : Screen("video_call")
    object Notification : Screen("notification")
    object Profile : Screen("profile")
} 