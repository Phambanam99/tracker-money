package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.devhunter9x.firstapp.api.ApiClient
import com.devhunter9x.firstapp.ui.screens.personal.PersonalDashboardScreen

@Composable
fun MainScreen(
        apiClient: ApiClient,
        onNavigateToRoomDetail: (String, String) -> Unit,
        onNavigateToCreateRoom: () -> Unit,
        onNavigateToJoinRoom: () -> Unit,
        onNavigateToAddPersonalExpense: () -> Unit,
        onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                            icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                            label = { Text("Groups") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Personal") },
                            label = { Text("Personal") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                    )
                }
            }
    ) { innerPadding ->
        when (selectedTab) {
            0 ->
                    RoomListScreen(
                            apiClient = apiClient,
                            onRoomSelected = onNavigateToRoomDetail,
                            onCreateRoom = onNavigateToCreateRoom,
                            onJoinRoom = onNavigateToJoinRoom,
                            onLogout = onLogout,
                            modifier = Modifier.padding(innerPadding)
                    )
            1 ->
                    PersonalDashboardScreen(
                            apiClient = apiClient,
                            onAddExpense = onNavigateToAddPersonalExpense,
                            // modifier = Modifier.padding(innerPadding) // PersonalDashboardScreen
                            // needs to accept modifier
                            )
        }
    }
}
