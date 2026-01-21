package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.*
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
        apiClient: ApiClient,
        onRegisterSuccess: () -> Unit,
        onNavigateBack: () -> Unit
) {
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
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
                Card(
                        modifier =
                                Modifier.fillMaxWidth(0.85f)
                                        .border(
                                                1.dp,
                                                Brush.verticalGradient(
                                                        listOf(
                                                                Color.White.copy(alpha = 0.1f),
                                                                Color.Transparent
                                                        )
                                                ),
                                                RoundedCornerShape(32.dp)
                                        ),
                        shape = RoundedCornerShape(32.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                )
                ) {
                        Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                Text(
                                        text = "Create Account",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text = "Join the money tracker",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Username") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        leadingIcon = {
                                                Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                        }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Password") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Password
                                                ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        leadingIcon = {
                                                Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                        }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        label = { Text("Confirm Password") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Password
                                                ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        leadingIcon = {
                                                Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                        },
                                        isError =
                                                confirmPassword.isNotEmpty() &&
                                                        password != confirmPassword
                                )

                                errorMessage?.let { error ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                                text = error,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                        )
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                        onClick = {
                                                if (password != confirmPassword) {
                                                        errorMessage = "Passwords do not match"
                                                        return@Button
                                                }
                                                scope.launch {
                                                        isLoading = true
                                                        errorMessage = null
                                                        val result =
                                                                apiClient.register(name, password)
                                                        result.fold(
                                                                onSuccess = { onRegisterSuccess() },
                                                                onFailure = { e: Throwable ->
                                                                        errorMessage =
                                                                                e.message
                                                                                        ?: "Registration failed"
                                                                }
                                                        )
                                                        isLoading = false
                                                }
                                        },
                                        enabled =
                                                name.isNotBlank() &&
                                                        password.isNotBlank() &&
                                                        password == confirmPassword &&
                                                        !isLoading,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.primary
                                                )
                                ) {
                                        if (isLoading) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                )
                                        } else {
                                                Text(
                                                        "Register",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                TextButton(onClick = onNavigateBack) {
                                        Text(
                                                "Already have an account? Login",
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                        }
                }
        }
}
