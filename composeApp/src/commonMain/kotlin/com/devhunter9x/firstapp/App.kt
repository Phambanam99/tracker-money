package com.devhunter9x.firstapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.devhunter9x.firstapp.api.ApiClient
import com.devhunter9x.firstapp.ui.*
import com.devhunter9x.firstapp.ui.screens.*

@Composable
fun App() {
    // Tạo ApiClient (thay đổi baseUrl cho phù hợp với môi trường)
    val apiClient = remember { 
        ApiClient(baseUrl = getBaseUrl())
    }
    
    var currentUserId by remember { mutableStateOf("") }
    
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val navController = rememberNavController()
            
            NavHost(
                navController = navController,
                startDestination = LoginRoute
            ) {
                composable<LoginRoute> {
                    LoginScreen(
                        apiClient = apiClient,
                        onLoginSuccess = { roomId, roomCode ->
                            navController.navigate(RoomDetailRoute(roomId, roomCode)) {
                                popUpTo(LoginRoute) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(RegisterRoute)
                        },
                        onNavigateToCreateRoom = {
                            navController.navigate(CreateRoomRoute)
                        }
                    )
                }
                
                composable<RegisterRoute> {
                    RegisterScreen(
                        apiClient = apiClient,
                        onRegisterSuccess = { roomId, roomCode ->
                            navController.navigate(RoomDetailRoute(roomId, roomCode)) {
                                popUpTo(LoginRoute) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable<CreateRoomRoute> {
                    CreateRoomScreen(
                        apiClient = apiClient,
                        onRoomCreated = { roomCode ->
                            // Sau khi tạo phòng, quay lại để đăng ký vào phòng
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable<RoomDetailRoute> { backStackEntry ->
                    val route: RoomDetailRoute = backStackEntry.toRoute()
                    RoomDetailScreen(
                        apiClient = apiClient,
                        roomId = route.roomId,
                        roomCode = route.roomCode,
                        onAddExpense = {
                            navController.navigate(AddExpenseRoute(route.roomId))
                        },
                        onLogout = {
                            apiClient.clearToken()
                            navController.navigate(LoginRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable<AddExpenseRoute> { backStackEntry ->
                    val route: AddExpenseRoute = backStackEntry.toRoute()
                    AddExpenseScreen(
                        apiClient = apiClient,
                        roomId = route.roomId,
                        currentUserId = currentUserId,
                        onExpenseAdded = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// Hàm lấy base URL tùy theo platform
expect fun getBaseUrl(): String