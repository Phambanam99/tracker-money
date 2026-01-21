package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhunter9x.firstapp.Room
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
        apiClient: ApiClient,
        onRoomSelected: (roomId: String, roomCode: String) -> Unit,
        onCreateRoom: () -> Unit,
        onJoinRoom: () -> Unit,
        onLogout: () -> Unit,
        modifier: Modifier = Modifier
) {
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = apiClient.getMyRooms()
        result.fold(onSuccess = { rooms = it }, onFailure = { errorMessage = it.message })
        isLoading = false
    }

    Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)) // Deep slate background
    ) {
        // Subtle background gradient
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.verticalGradient(
                                                colors =
                                                        listOf(
                                                                Color(0xFF1E293B)
                                                                        .copy(alpha = 0.5f),
                                                                Color(0xFF0F172A)
                                                        )
                                        )
                                )
        )

        Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                            title = {
                                Text(
                                        "Phòng của tôi",
                                        style =
                                                MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                )
                                )
                            },
                            colors =
                                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                            titleContentColor = Color.White
                                    ),
                            actions = {
                                IconButton(onClick = onLogout) {
                                    Icon(
                                            Icons.Default.Logout,
                                            contentDescription = "Đăng xuất",
                                            tint = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            },
                            modifier =
                                    Modifier.clip(
                                            RoundedCornerShape(
                                                    bottomStart = 24.dp,
                                                    bottomEnd = 24.dp
                                            )
                                    )
                    )
                },
                floatingActionButton = {
                    Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        ExtendedFloatingActionButton(
                                onClick = onJoinRoom,
                                containerColor = Color(0xFF334155),
                                contentColor = Color.White,
                                icon = { Icon(Icons.Default.Link, "Join") },
                                text = { Text("Tham gia") },
                                shape = RoundedCornerShape(16.dp)
                        )
                        ExtendedFloatingActionButton(
                                onClick = onCreateRoom,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                icon = { Icon(Icons.Default.Add, "Create") },
                                text = { Text("Tạo phòng") },
                                shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Responsive container
                Box(
                        modifier =
                                Modifier.fillMaxWidth(
                                                if (Modifier.fillMaxSize().let { true }) 1f
                                                else 0.8f
                                        ) // Simplified for KMP
                                        .align(Alignment.TopCenter)
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                        errorMessage != null -> {
                            ErrorState(
                                    message = errorMessage ?: "Lỗi không xác định",
                                    onRetry = {
                                        scope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            val result = apiClient.getMyRooms()
                                            result.fold(
                                                    onSuccess = { rooms = it },
                                                    onFailure = { errorMessage = it.message }
                                            )
                                            isLoading = false
                                        }
                                    }
                            )
                        }
                        rooms.isEmpty() -> {
                            EmptyState(onJoinRoom, onCreateRoom)
                        }
                        else -> {
                            LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp, 24.dp, 16.dp, 80.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(rooms) { room ->
                                    RoomCard(
                                            room = room,
                                            onClick = { onRoomSelected(room.id, room.code) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onClick)
                            .border(
                                    1.dp,
                                    Brush.linearGradient(
                                            listOf(
                                                    Color.White.copy(alpha = 0.2f),
                                                    Color.Transparent
                                            )
                                    ),
                                    RoundedCornerShape(20.dp)
                            ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f))
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                    modifier =
                            Modifier.size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = room.name,
                        style =
                                MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = "Mã: ${room.code}",
                            style =
                                    MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.6f)
                                    )
                    )
                }
            }

            Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun EmptyState(onJoinRoom: () -> Unit, onCreateRoom: () -> Unit) {
    Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Box(
                modifier =
                        Modifier.size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    Icons.Default.HomeWork,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
                text = "Chưa có phòng nào",
                style =
                        MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                        )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
                text =
                        "Hãy tạo phòng mới hoặc tham gia phòng của bạn bè để bắt đầu quản lý chi tiêu.",
                style =
                        MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                        )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
                onClick = onCreateRoom,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tạo phòng mới")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
                onClick = onJoinRoom,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Icon(Icons.Default.Link, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tham gia bằng mã")
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
                text = message,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onRetry) { Text("Thử lại", fontWeight = FontWeight.Bold) }
    }
}
