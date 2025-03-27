package com.example.dacs3.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(
    val title: String,
    val icon: Int
) {
    object Chat : BottomNavItem("Nhắn tin", com.example.dacs3.R.drawable.ic_chat)
    object RandomCall : BottomNavItem("Mạng xã hội", com.example.dacs3.R.drawable.ic_random_call)
    object VideoCall : BottomNavItem("Video Call", com.example.dacs3.R.drawable.ic_video_call)
    object Notification : BottomNavItem("Thông báo", com.example.dacs3.R.drawable.ic_notification)
    object Profile : BottomNavItem("Cá nhân", com.example.dacs3.R.drawable.ic_profile)
}

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit
) {
    var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Chat) }

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem.Chat,
            BottomNavItem.RandomCall,
            BottomNavItem.VideoCall,
            BottomNavItem.Notification,
            BottomNavItem.Profile
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = {
                    selectedItem = item
                    onItemClick(item)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title
                    )
                },
                label = { 
                    Text(
                        text = item.title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    }
} 