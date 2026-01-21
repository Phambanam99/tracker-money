package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun CreateRoomScreen(
    apiClient: ApiClient,
    onRoomCreated: (roomCode: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏠 Tạo phòng mới",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("Tên phòng (VD: Phòng 101)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = roomCode,
            onValueChange = { roomCode = it.uppercase() },
            label = { Text("Mã phòng (VD: P101)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Mã phòng để bạn bè tham gia") }
        )
        
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        successMessage?.let { success ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = success,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    val result = apiClient.createRoom(roomName, roomCode)
                    result.fold(
                        onSuccess = { room ->
                            successMessage = "Phòng '${room.name}' đã được tạo! Mã: ${room.code}"
                            onRoomCreated(room.code)
                        },
                        onFailure = { e ->
                            errorMessage = e.message ?: "Tạo phòng thất bại"
                        }
                    )
                    isLoading = false
                }
            },
            enabled = roomName.isNotBlank() && roomCode.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Tạo phòng")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateBack) {
            Text("← Quay lại")
        }
    }
}
