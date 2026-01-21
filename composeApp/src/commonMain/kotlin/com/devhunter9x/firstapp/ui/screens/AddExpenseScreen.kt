package com.devhunter9x.firstapp.ui.screens

import User
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    apiClient: ApiClient,
    roomId: String,
    currentUserId: String,
    onExpenseAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPayerId by remember { mutableStateOf(currentUserId) }
    var selectedParticipants by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(roomId) {
        apiClient.getMembers(roomId).onSuccess { 
            members = it
            // Mặc định chọn tất cả thành viên
            selectedParticipants = it.map { m -> m.id }.toSet()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm chi tiêu") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Số tiền
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền (VNĐ)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Text("💰") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mô tả
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả (VD: Mua nước, Tiền điện...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Text("📝") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Người trả tiền
            Text(
                text = "Người trả tiền:",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(0.3f)
            ) {
                items(members) { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPayerId == member.id,
                            onClick = { selectedPayerId = member.id }
                        )
                        Text(member.name)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Người tham gia
            Text(
                text = "Người tham gia (chia tiền):",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(0.4f)
            ) {
                items(members) { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedParticipants.contains(member.id),
                            onCheckedChange = { checked ->
                                selectedParticipants = if (checked) {
                                    selectedParticipants + member.id
                                } else {
                                    selectedParticipants - member.id
                                }
                            }
                        )
                        Text(member.name)
                    }
                }
            }
            
            // Preview
            if (amount.isNotEmpty() && selectedParticipants.isNotEmpty()) {
                val amountValue = amount.toDoubleOrNull() ?: 0.0
                val perPerson = amountValue / selectedParticipants.size
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Mỗi người: ${String.format("%.0f", perPerson)}đ",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Submit button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        errorMessage = "Vui lòng nhập số tiền hợp lệ"
                        return@Button
                    }
                    if (description.isBlank()) {
                        errorMessage = "Vui lòng nhập mô tả"
                        return@Button
                    }
                    if (selectedParticipants.isEmpty()) {
                        errorMessage = "Vui lòng chọn ít nhất một người tham gia"
                        return@Button
                    }
                    
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = apiClient.createExpense(
                            roomId = roomId,
                            payerId = selectedPayerId,
                            amount = amountValue,
                            description = description,
                            participantIds = selectedParticipants.toList()
                        )
                        result.fold(
                            onSuccess = { onExpenseAdded() },
                            onFailure = { e -> errorMessage = e.message ?: "Thêm chi tiêu thất bại" }
                        )
                        isLoading = false
                    }
                },
                enabled = amount.isNotBlank() && description.isNotBlank() && 
                         selectedParticipants.isNotEmpty() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Thêm chi tiêu")
                }
            }
        }
    }
}
