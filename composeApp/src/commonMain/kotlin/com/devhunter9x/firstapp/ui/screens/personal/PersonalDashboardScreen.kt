package com.devhunter9x.firstapp.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.devhunter9x.firstapp.generated.resources.*
import com.devhunter9x.firstapp.util.formatCurrency
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

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
                result.fold(
                        onSuccess = { expenses = it },
                        onFailure = { errorMessage = it.message }
                )
                isLoading = false
        }

        val totalIncome = expenses.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense =
                expenses.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) { // Slate 900
                // Background Gradient
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(
                                                Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        Color(0xFF1E293B)
                                                                                .copy(
                                                                                        alpha = 0.5f
                                                                                ), // Slate 800
                                                                        Color(
                                                                                0xFF0F172A
                                                                        ) // Slate 900
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
                                                        stringResource(Res.string.personal_finance),
                                                        style =
                                                                MaterialTheme.typography.titleLarge
                                                                        .copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                color = Color.White
                                                                        )
                                                )
                                        },
                                        colors =
                                                TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                        containerColor =
                                                                Color(0xFF1E293B)
                                                                        .copy(
                                                                                alpha = 0.7f
                                                                        ) // Slate 800
                                                )
                                )
                        },
                        floatingActionButton = {
                                FloatingActionButton(
                                        onClick = onAddExpense,
                                        containerColor = Color(0xFF7C3AED), // Violet 600
                                        contentColor = Color.White
                                ) {
                                        Icon(
                                                Icons.Default.Add,
                                                contentDescription =
                                                        stringResource(Res.string.add_expense)
                                        )
                                }
                        }
                ) { padding ->
                        if (isLoading) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = Color(0xFF7C3AED)) }
                        } else if (errorMessage != null) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                stringResource(
                                                        Res.string.error_prefix,
                                                        errorMessage ?: ""
                                                ),
                                                color = MaterialTheme.colorScheme.error
                                        )
                                }
                        } else {
                                BoxWithConstraints {
                                        if (maxWidth >= 800.dp) {
                                                // Desktop/Wide Layout (Pro Max)
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(padding)
                                                                        .padding(24.dp),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(24.dp)
                                                ) {
                                                        // Left Column: Balance & Stats
                                                        Column(
                                                                modifier = Modifier.weight(0.4f),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(24.dp)
                                                        ) {
                                                                BalanceCard(
                                                                        balance,
                                                                        totalIncome,
                                                                        totalExpense
                                                                )
                                                                // Placeholder for Chart or other
                                                                // stats
                                                                Card(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .height(
                                                                                                200.dp
                                                                                        ),
                                                                        colors =
                                                                                CardDefaults
                                                                                        .cardColors(
                                                                                                containerColor =
                                                                                                        Color(
                                                                                                                        0xFF1E293B
                                                                                                                )
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.5f
                                                                                                                )
                                                                                        )
                                                                ) {
                                                                        Box(
                                                                                modifier =
                                                                                        Modifier.fillMaxSize(),
                                                                                contentAlignment =
                                                                                        Alignment
                                                                                                .Center
                                                                        ) {
                                                                                Text(
                                                                                        stringResource(
                                                                                                Res.string
                                                                                                        .analytics_coming_soon
                                                                                        ),
                                                                                        color =
                                                                                                Color.White
                                                                                                        .copy(
                                                                                                                0.5f
                                                                                                        )
                                                                                )
                                                                        }
                                                                }
                                                        }

                                                        // Right Column: Transactions
                                                        Column(modifier = Modifier.weight(0.6f)) {
                                                                Text(
                                                                        stringResource(
                                                                                Res.string
                                                                                        .recent_transactions
                                                                        ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .headlineSmall
                                                                                        .copy(
                                                                                                color =
                                                                                                        Color.White,
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        bottom =
                                                                                                16.dp
                                                                                )
                                                                )
                                                                TransactionList(expenses)
                                                        }
                                                }
                                        } else {
                                                // Mobile Layout
                                                LazyColumn(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(padding),
                                                        contentPadding = PaddingValues(16.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        item {
                                                                BalanceCard(
                                                                        balance,
                                                                        totalIncome,
                                                                        totalExpense
                                                                )
                                                        }
                                                        item {
                                                                Text(
                                                                        stringResource(
                                                                                Res.string
                                                                                        .recent_transactions
                                                                        ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .titleMedium
                                                                                        .copy(
                                                                                                color =
                                                                                                        Color.White,
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        vertical =
                                                                                                8.dp
                                                                                )
                                                                )
                                                        }
                                                        if (expenses.isEmpty()) {
                                                                item { EmptyTransactionMessage() }
                                                        } else {
                                                                items(expenses) { expense ->
                                                                        PersonalExpenseItem(expense)
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
fun TransactionList(expenses: List<PersonalExpense>) {
        LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
        ) {
                if (expenses.isEmpty()) {
                        item { EmptyTransactionMessage() }
                } else {
                        items(expenses) { expense -> PersonalExpenseItem(expense) }
                }
        }
}

@Composable
fun EmptyTransactionMessage() {
        Text(
                stringResource(Res.string.no_transactions),
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
}

@Composable
fun BalanceCard(balance: Double, totalIncome: Double, totalExpense: Double) {
        Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
                Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                stringResource(Res.string.total_balance),
                                style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                                color = Color.White.copy(0.7f)
                                        )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                                "${balance.formatCurrency()}${stringResource(Res.string.currency_symbol)}",
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
                                        label = stringResource(Res.string.income),
                                        amount = totalIncome,
                                        color = Color(0xFF10B981), // Emerald 500
                                        icon = Icons.Default.ArrowUpward
                                )
                                BalanceItem(
                                        label = stringResource(Res.string.expense),
                                        amount = totalExpense,
                                        color = Color(0xFFEF4444), // Red 500
                                        icon = Icons.Default.ArrowDownward
                                )
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
                                style =
                                        MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White.copy(0.7f)
                                        )
                        )
                }
                Text(
                        "${amount.formatCurrency()}${stringResource(Res.string.currency_symbol)}",
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
                colors =
                        CardDefaults.cardColors(
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                        )
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
                                        if (isIncome && !expense.source.isNullOrBlank()) {
                                                "${expense.category.name} • ${expense.source}"
                                        } else {
                                                expense.category.name
                                        },
                                        style =
                                                MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White.copy(0.5f)
                                                )
                                )
                        }
                        Text(
                                "$prefix${expense.amount.formatCurrency()}${stringResource(Res.string.currency_symbol)}",
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
@OptIn(ExperimentalTime::class)
fun formatDate(timestamp: Long): String {
        // In a real app, use a proper date formatter
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"
}
