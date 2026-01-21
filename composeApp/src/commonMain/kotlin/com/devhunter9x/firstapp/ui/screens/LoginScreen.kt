package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhunter9x.firstapp.*
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
        apiClient: ApiClient,
        onLoginSuccess: (userId: String) -> Unit,
        onNavigateToRegister: () -> Unit
) {
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.verticalGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme
                                                                        .background,
                                                                MaterialTheme.colorScheme.surface
                                                        )
                                        )
                                ),
                contentAlignment = Alignment.Center
        ) {
                Column(
                        modifier =
                                Modifier.widthIn(max = 600.dp)
                                        .fillMaxWidth(0.9f)
                                        .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        // Header Section
                        Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                                text = "TrackerMoney",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = 2.sp
                        )

                        Text(
                                text = "Smart Expense Management",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Login Card
                        Surface(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                tonalElevation = 8.dp
                        ) {
                                Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = "Đăng nhập",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Start,
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(bottom = 24.dp)
                                        )

                                        OutlinedTextField(
                                                value = name,
                                                onValueChange = { name = it },
                                                label = { Text("Tên đăng nhập") },
                                                placeholder = { Text("username") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = {
                                                        Icon(
                                                                Icons.Default.Person,
                                                                contentDescription = null
                                                        )
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        OutlinedTextField(
                                                value = password,
                                                onValueChange = { password = it },
                                                label = { Text("Mật khẩu") },
                                                singleLine = true,
                                                visualTransformation =
                                                        PasswordVisualTransformation(),
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Password
                                                        ),
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = {
                                                        Icon(
                                                                Icons.Default.Lock,
                                                                contentDescription = null
                                                        )
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                        )

                                        errorMessage?.let { error ->
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                        text = error,
                                                        color = MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(32.dp))

                                        Button(
                                                onClick = {
                                                        scope.launch {
                                                                isLoading = true
                                                                errorMessage = null
                                                                val result =
                                                                        apiClient.login(
                                                                                name,
                                                                                password
                                                                        )
                                                                result.fold(
                                                                        onSuccess = {
                                                                                authResponse:
                                                                                        AuthResponse
                                                                                ->
                                                                                onLoginSuccess(
                                                                                        authResponse
                                                                                                .user
                                                                                                .id
                                                                                )
                                                                        },
                                                                        onFailure = { e: Throwable
                                                                                ->
                                                                                errorMessage =
                                                                                        e.message
                                                                                                ?: "Đăng nhập thất bại"
                                                                        }
                                                                )
                                                                isLoading = false
                                                        }
                                                },
                                                enabled =
                                                        name.isNotBlank() &&
                                                                password.isNotBlank() &&
                                                                !isLoading,
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                contentColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary
                                                        )
                                        ) {
                                                if (isLoading) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary,
                                                                strokeWidth = 2.dp
                                                        )
                                                } else {
                                                        Text(
                                                                "Đăng nhập",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        TextButton(
                                                onClick = onNavigateToRegister,
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                                Text(
                                                        "Chưa có tài khoản? Đăng ký ngay",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.bodyMedium
                                                )
                                        }
                                }
                        }
                }
        }
}
