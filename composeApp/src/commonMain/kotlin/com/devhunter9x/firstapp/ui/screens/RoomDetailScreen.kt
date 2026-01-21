package com.devhunter9x.firstapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.*
import com.devhunter9x.firstapp.api.ApiClient
import com.devhunter9x.firstapp.util.formatNoDecimals
import com.devhunter9x.firstapp.util.formatTimestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
        apiClient: ApiClient,
        roomId: String,
        roomCode: String,
        currentUserId: String,
        onAddExpense: () -> Unit,
        onBack: () -> Unit,
        onLogout: () -> Unit
) {
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var balances by remember { mutableStateOf<List<Balance>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    fun refreshData() {
        scope.launch {
            isLoading = true
            apiClient.getMembers(roomId).onSuccess { members = it }
            apiClient.getExpenses(roomId).onSuccess { expenses = it }
            apiClient.getBalances(roomId).onSuccess { balances = it }
            isLoading = false
        }
    }

    LaunchedEffect(roomId, refreshTrigger) { refreshData() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                            colors =
                                    TopAppBarDefaults.topAppBarColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.background.copy(
                                                            alpha = 0.9f
                                                    )
                                    ),
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            title = {
                                Column {
                                    Text(
                                            text = "Room #$roomCode",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                            text = "${members.size} members",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = onLogout) {
                                    Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = "Logout",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                            onClick = onAddExpense,
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
        ) { padding ->
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Persistent Side rail for wide screens could be added here
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            divider = {
                                HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            }
                    ) {
                        Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text("Chi tiêu", style = MaterialTheme.typography.labelLarge)
                                },
                                icon = {
                                    Icon(
                                            Icons.Default.List,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                    )
                                }
                        )
                        Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text("Công nợ", style = MaterialTheme.typography.labelLarge)
                                },
                                icon = {
                                    Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                    )
                                }
                        )
                        Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Text("Thành viên", style = MaterialTheme.typography.labelLarge)
                                },
                                icon = {
                                    Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                    )
                                }
                        )
                    }

                    if (isLoading) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                    } else {
                        when (selectedTab) {
                            0 -> ExpensesList(expenses, members)
                            1 ->
                                    BalancesList(
                                            balances = balances,
                                            currentUserId = currentUserId,
                                            apiClient = apiClient,
                                            roomId = roomId,
                                            onSettled = { refreshTrigger++ }
                                    )
                            2 -> MembersList(members)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpensesList(expenses: List<Expense>, members: List<User>) {
    val memberMap = members.associateBy { it.id }
    val sortedExpenses = expenses.sortedByDescending { it.timestamp.toLongOrNull() ?: 0L }

    if (expenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = "No expenses yet\nTap + to add one",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { items(sortedExpenses, key = { it.id }) { expense -> ExpenseCard(expense, memberMap) } }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, memberMap: Map<String, User>) {
    val payerName = memberMap[expense.payerId]?.name ?: "Unknown"
    var isExpanded by remember { mutableStateOf(false) }

    val dateText =
            try {
                val timestamp = expense.timestamp.toLongOrNull() ?: 0L
                if (timestamp > 0) timestamp.formatTimestamp() else expense.timestamp
            } catch (e: Exception) {
                expense.timestamp
            }

    Card(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                    ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                ) {
                    Box(
                            modifier =
                                    Modifier.size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.1f
                                                    )
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                                text = expense.description,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    size = 12.dp,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                            text = "${expense.amount.formatNoDecimals()}đ",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                            text = "by $payerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "Split between ${expense.participantAmounts.size} people",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    expense.participantAmounts.forEach { (userId, amount) ->
                        val name = memberMap[userId]?.name ?: "Unknown"
                        val isPayer = userId == expense.payerId

                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                            if (isPayer) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                    text = "${amount.formatNoDecimals()}đ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color =
                                            if (isPayer) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// Extension to use Icon with Dp size
@Composable
private fun Icon(
        imageVector: ImageVector,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        size: androidx.compose.ui.unit.Dp,
        tint: androidx.compose.ui.graphics.Color = LocalContentColor.current
) {
    Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            tint = tint
    )
}

@Composable
private fun BalancesList(
        balances: List<Balance>,
        currentUserId: String,
        apiClient: ApiClient,
        roomId: String,
        onSettled: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var settlingBalance by remember { mutableStateOf<Balance?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isSettling by remember { mutableStateOf(false) }

    if (balances.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = "Everything's settled!\nNo debts found.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(balances) { balance ->
                val isCreditor = balance.toUser.id == currentUserId

                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                ),
                        border =
                                BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                        modifier =
                                                Modifier.size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                MaterialTheme.colorScheme.error
                                                                        .copy(alpha = 0.1f)
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                            text =
                                                    "${balance.fromUser.name} → ${balance.toUser.name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                            text =
                                                    if (isCreditor) "Owes you"
                                                    else "Owes ${balance.toUser.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                    text = "${balance.amount.formatNoDecimals()}đ",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isCreditor) {
                            Button(
                                    onClick = {
                                        settlingBalance = balance
                                        showConfirmDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.primary
                                            ),
                                    enabled = !isSettling
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, size = 18.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm Received")
                            }
                        } else {
                            OutlinedButton(
                                    onClick = {},
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = false
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, size = 18.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Waiting for confirmation")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog && settlingBalance != null) {
        AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    settlingBalance = null
                },
                title = { Text("Confirm Payment") },
                text = {
                    Text(
                            "Confirm you've received ${settlingBalance!!.amount.formatNoDecimals()}đ from ${settlingBalance!!.fromUser.name}?"
                    )
                },
                confirmButton = {
                    Button(
                            onClick = {
                                scope.launch {
                                    isSettling = true
                                    val balance = settlingBalance!!

                                    val result =
                                            apiClient.createExpense(
                                                    roomId = roomId,
                                                    payerId = balance.fromUser.id,
                                                    amount = balance.amount,
                                                    description =
                                                            "Trả nợ cho ${balance.toUser.name}",
                                                    participantAmounts =
                                                            mapOf(
                                                                    balance.toUser.id to
                                                                            balance.amount
                                                            ),
                                                    splitEqually = false
                                            )

                                    result.fold(
                                            onSuccess = {
                                                showConfirmDialog = false
                                                settlingBalance = null
                                                onSettled()
                                            },
                                            onFailure = {}
                                    )
                                    isSettling = false
                                }
                            },
                            enabled = !isSettling
                    ) {
                        if (isSettling) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Confirm")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                            onClick = {
                                showConfirmDialog = false
                                settlingBalance = null
                            }
                    ) { Text("Cancel") }
                }
        )
    }
}

@Composable
private fun MembersList(members: List<User>) {
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(members) { member ->
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                            ),
                    border =
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                            modifier =
                                    Modifier.size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    MaterialTheme.colorScheme.secondary.copy(
                                                            alpha = 0.1f
                                                    )
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = "Thành viên",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
