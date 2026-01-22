package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
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
import com.devhunter9x.firstapp.*
import com.devhunter9x.firstapp.api.ApiClient
import com.devhunter9x.firstapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomScreen(
        apiClient: ApiClient,
        onJoinSuccess: (roomId: String, roomCode: String) -> Unit,
        onNavigateBack: () -> Unit
) {
        var roomCode by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        val joinRoomFailedMessage = stringResource(Res.string.join_room_failed)

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
                // Background Gradient
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
                                TopAppBar(
                                        title = {
                                                Text(
                                                        stringResource(Res.string.join_room_title),
                                                        fontWeight = FontWeight.Bold
                                                )
                                        },
                                        navigationIcon = {
                                                IconButton(onClick = onNavigateBack) {
                                                        Icon(
                                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                                contentDescription =
                                                                        stringResource(
                                                                                Res.string.back
                                                                        ),
                                                                tint = Color.White
                                                        )
                                                }
                                        },
                                        colors =
                                                TopAppBarDefaults.topAppBarColors(
                                                        containerColor = Color.Transparent,
                                                        titleContentColor = Color.White
                                                )
                                )
                        }
                ) { padding ->
                        Box(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                contentAlignment = Alignment.Center
                        ) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxWidth(
                                                                if (Modifier.fillMaxSize().let {
                                                                                true
                                                                        }
                                                                )
                                                                        0.9f
                                                                else 0.4f
                                                        )
                                                        .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Box(
                                                modifier =
                                                        Modifier.size(100.dp)
                                                                .clip(RoundedCornerShape(24.dp))
                                                                .background(
                                                                        MaterialTheme.colorScheme
                                                                                .primary.copy(
                                                                                alpha = 0.1f
                                                                        )
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Icon(
                                                        Icons.Default.Link,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(48.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(32.dp))

                                        Text(
                                                text = stringResource(Res.string.join_with_code),
                                                style =
                                                        MaterialTheme.typography.headlineMedium
                                                                .copy(
                                                                        fontWeight =
                                                                                FontWeight
                                                                                        .ExtraBold,
                                                                        color = Color.White
                                                                )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                                text = stringResource(Res.string.join_room_hint),
                                                style =
                                                        MaterialTheme.typography.bodyLarge.copy(
                                                                color =
                                                                        Color.White.copy(
                                                                                alpha = 0.6f
                                                                        ),
                                                                textAlign = TextAlign.Center,
                                                                lineHeight = 24.sp
                                                        )
                                        )

                                        Spacer(modifier = Modifier.height(48.dp))

                                        OutlinedTextField(
                                                value = roomCode,
                                                onValueChange = { roomCode = it.uppercase() },
                                                label = {
                                                        Text(
                                                                stringResource(
                                                                        Res.string.room_code_label
                                                                )
                                                        )
                                                },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors =
                                                        OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                unfocusedBorderColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.1f
                                                                        ),
                                                                focusedLabelColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                unfocusedLabelColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.4f
                                                                        ),
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White
                                                        )
                                        )

                                        if (errorMessage != null) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                        text = errorMessage!!,
                                                        color = MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Start
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(32.dp))

                                        Button(
                                                onClick = {
                                                        scope.launch {
                                                                isLoading = true
                                                                errorMessage = null
                                                                val result =
                                                                        apiClient.joinRoom(roomCode)
                                                                result.fold(
                                                                        onSuccess = {
                                                                                joinResponse:
                                                                                        JoinRoomResponse
                                                                                ->
                                                                                onJoinSuccess(
                                                                                        joinResponse
                                                                                                .room
                                                                                                .id,
                                                                                        joinResponse
                                                                                                .room
                                                                                                .code
                                                                                )
                                                                        },
                                                                        onFailure = { e: Throwable
                                                                                ->
                                                                                errorMessage =
                                                                                        e.message
                                                                                                ?: joinRoomFailedMessage
                                                                        }
                                                                )
                                                                isLoading = false
                                                        }
                                                },
                                                enabled = roomCode.isNotBlank() && !isLoading,
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                disabledContainerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary.copy(
                                                                                alpha = 0.3f
                                                                        )
                                                        )
                                        ) {
                                                if (isLoading) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                color = Color.White,
                                                                strokeWidth = 2.dp
                                                        )
                                                } else {
                                                        Text(
                                                                stringResource(
                                                                        Res.string.join_room_button
                                                                ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium.copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        )
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}
