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
import com.devhunter9x.firstapp.ui.screens.personal.*
import com.devhunter9x.firstapp.ui.theme.TrackerMoneyTheme

@Composable
fun App() {
    // Tạo ApiClient (thay đổi baseUrl cho phù hợp với môi trường)
    val apiClient = remember { ApiClient(baseUrl = getBaseUrl()) }

    var currentUserId by remember { mutableStateOf("") }

    TrackerMoneyTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = LoginRoute) {
                // Login - không cần roomCode
                composable<LoginRoute> {
                    LoginScreen(
                            apiClient = apiClient,
                            onLoginSuccess = { userId ->
                                currentUserId = userId
                                // Sau khi login, đi đến MainScreen
                                navController.navigate(MainRoute) {
                                    popUpTo(LoginRoute) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate(RegisterRoute) }
                    )
                }

                // Register - không cần roomCode
                composable<RegisterRoute> {
                    RegisterScreen(
                            apiClient = apiClient,
                            onRegisterSuccess = {
                                // Sau khi register, đi đến danh sách phòng
                                navController.navigate(RoomListRoute) {
                                    popUpTo(LoginRoute) { inclusive = true }
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Main Screen (Bottom Nav)
                composable<MainRoute> {
                    MainScreen(
                            apiClient = apiClient,
                            onNavigateToRoomDetail = { roomId, roomCode ->
                                navController.navigate(RoomDetailRoute(roomId, roomCode))
                            },
                            onNavigateToCreateRoom = { navController.navigate(CreateRoomRoute) },
                            onNavigateToJoinRoom = { navController.navigate(JoinRoomRoute) },
                            onNavigateToAddPersonalExpense = {
                                navController.navigate(AddPersonalExpenseRoute)
                            },
                            onLogout = {
                                apiClient.clearToken()
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                    )
                }

                // Tạo phòng mới
                composable<CreateRoomRoute> {
                    CreateRoomScreen(
                            apiClient = apiClient,
                            onRoomCreated = { roomCode ->
                                // Sau khi tạo phòng, quay lại danh sách
                                navController.popBackStack()
                            },
                            onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Join phòng bằng mã
                composable<JoinRoomRoute> {
                    JoinRoomScreen(
                            apiClient = apiClient,
                            onJoinSuccess = { roomId, roomCode ->
                                navController.navigate(RoomDetailRoute(roomId, roomCode)) {
                                    popUpTo(RoomListRoute)
                                }
                            },
                            onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Chi tiết phòng
                composable<RoomDetailRoute> { backStackEntry ->
                    val route: RoomDetailRoute = backStackEntry.toRoute()
                    RoomDetailScreen(
                            apiClient = apiClient,
                            roomId = route.roomId,
                            roomCode = route.roomCode,
                            currentUserId = currentUserId,
                            onAddExpense = {
                                navController.navigate(AddExpenseRoute(route.roomId))
                            },
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                apiClient.clearToken()
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                    )
                }

                // Thêm chi tiêu
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

                // Add Personal Expense
                composable<AddPersonalExpenseRoute> {
                    AddPersonalExpenseScreen(
                            apiClient = apiClient,
                            onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// Hàm lấy base URL tùy theo platform
expect fun getBaseUrl(): String
