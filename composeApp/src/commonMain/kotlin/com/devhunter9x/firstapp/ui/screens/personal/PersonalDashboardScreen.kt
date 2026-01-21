package com.devhunter9x.firstapp.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.*
import com.devhunter9x.firstapp.api.ApiClient
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDashboardScreen(
        apiClient: ApiClient,
        onAddExpense: () -> Unit,
        modifier: Modifier = Modifier
) {
    var expenses by remember { mutableStateOf<List<PersonalExpense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        val result = apiClient.getPersonalExpenses()
        result.fold(onSuccess = { expenses = it }, onFailure = { errorMessage = it.message })
        isLoading = false
    }

    val totalIncome = expenses.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = expenses.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
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
                                        "Personal Finance",
                                        style =
                                                MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                )
                                )
                            },
                            colors =
                                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
                                    )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                            onClick = onAddExpense,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                    ) { Icon(Icons.Default.Add, contentDescription = "Add Expense") }
                }
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Balance Card
                    item {
                        Card(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                        "Total Balance",
                                        style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                        color = Color.White.copy(0.7f)
                                                )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        "$${balance}", // Format appropriately
                                        style =
                                                MaterialTheme.typography.displayMedium.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                )
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    BalanceItem(
                                            label = "Income",
                                            amount = totalIncome,
                                            color = Color(0xFF10B981), // Emerald 500
                                            icon = Icons.Default.ArrowUpward
                                    )
                                    BalanceItem(
                                            label = "Expense",
                                            amount = totalExpense,
                                            color = Color(0xFFEF4444), // Red 500
                                            icon = Icons.Default.ArrowDownward
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                                "Recent Transactions",
                                style =
                                        MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                        ),
                                modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (expenses.isEmpty()) {
                        item {
                            Text(
                                    "No transactions yet.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(expenses) { expense -> PersonalExpenseItem(expense) }
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceItem(
        label: String,
        amount: Double,
        color: Color,
        icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                    label,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(0.7f))
            )
        }
        Text(
                "$${amount}",
                style =
                        MaterialTheme.typography.titleMedium.copy(
                                color = color,
                                fontWeight = FontWeight.Bold
                        )
        )
    }
}

@Composable
fun PersonalExpenseItem(expense: PersonalExpense) {
    val isIncome = expense.type == TransactionType.INCOME
    val amountColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
    val prefix = if (isIncome) "+" else "-"

    Card(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
    ) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                        expense.note.ifBlank {
                            expense.category.name
                        }, // Fallback to category name if note is empty
                        style =
                                MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                )
                )
                Text(
                        formatDate(expense.date),
                        style =
                                MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(0.5f)
                                )
                )
                Text(
                        expense.category.name,
                        style =
                                MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(0.5f)
                                )
                )
            }
            Text(
                    "$prefix$${expense.amount}",
                    style =
                            MaterialTheme.typography.titleMedium.copy(
                                    color = amountColor,
                                    fontWeight = FontWeight.Bold
                            )
            )
        }
    }
}

// Simple date formatter placeholder
fun formatDate(timestamp: Long): String {
    // In a real app, use a proper date formatter
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"
}
