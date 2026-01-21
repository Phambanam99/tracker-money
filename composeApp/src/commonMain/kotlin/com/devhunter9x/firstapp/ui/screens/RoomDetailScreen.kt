package com.devhunter9x.firstapp.ui.screens

import Balance
import Expense
import User
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    apiClient: ApiClient,
    roomId: String,
    roomCode: String,
    onAddExpense: () -> Unit,
    onLogout: () -> Unit
) {
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var balances by remember { mutableStateOf<List<Balance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(roomId) {
        isLoading = true
        // Load members
        apiClient.getMembers(roomId).onSuccess { members = it }
        // Load expenses
        apiClient.getExpenses(roomId).onSuccess { expenses = it }
        // Load balances
        apiClient.getBalances(roomId).onSuccess { balances = it }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Phòng $roomCode")
                        Text(
                            text = "${members.size} thành viên",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Đăng xuất")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("➕", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Chi tiêu") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Công nợ") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Thành viên") }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> ExpensesList(expenses, members)
                    1 -> BalancesList(balances)
                    2 -> MembersList(members)
                }
            }
        }
    }
}

@Composable
private fun ExpensesList(expenses: List<Expense>, members: List<User>) {
    val memberMap = members.associateBy { it.id }
    
    if (expenses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có chi tiêu nào\nBấm + để thêm mới",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(expenses) { expense ->
                ExpenseCard(expense, memberMap)
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, memberMap: Map<String, User>) {
    val payerName = memberMap[expense.payerId]?.name ?: "Unknown"
    val participantNames = expense.participantIds.mapNotNull { memberMap[it]?.name }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.0f", expense.amount)}đ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "💰 $payerName đã trả",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "👥 ${participantNames.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BalancesList(balances: List<Balance>) {
    if (balances.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎉 Không ai nợ ai!\nMọi người đã chi tiêu công bằng",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(balances) { balance ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${balance.fromUser.name} → ${balance.toUser.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${balance.fromUser.name} nợ ${balance.toUser.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "${String.format("%.0f", balance.amount)}đ",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersList(members: List<User>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(members) { member ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
