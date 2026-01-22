package com.devhunter9x.firstapp.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.CreatePersonalExpenseRequest
import com.devhunter9x.firstapp.PersonalCategory
import com.devhunter9x.firstapp.TransactionType
import com.devhunter9x.firstapp.api.ApiClient
import com.devhunter9x.firstapp.generated.resources.*
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddPersonalExpenseScreen(apiClient: ApiClient, onNavigateBack: () -> Unit) {
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("") } // Nguồn thu
        var type by remember { mutableStateOf(TransactionType.EXPENSE) }
        var category by remember { mutableStateOf(PersonalCategory.OTHER) }
        var expanded by remember { mutableStateOf(false) } // For category dropdown
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        val invalidAmountMessage = stringResource(Res.string.invalid_amount)

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
                                .background(Color(0xFF0F172A)) // Fallback/base
        ) {
                Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                                CenterAlignedTopAppBar(
                                        title = {
                                                Text(
                                                        stringResource(Res.string.add_transaction),
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
                                        navigationIcon = {
                                                IconButton(onClick = onNavigateBack) {
                                                        Icon(
                                                                Icons.Default.ArrowBack,
                                                                contentDescription =
                                                                        stringResource(
                                                                                Res.string.back
                                                                        ),
                                                                tint = Color.White
                                                        )
                                                }
                                        },
                                        colors =
                                                TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                        containerColor =
                                                                Color(0xFF1E293B).copy(alpha = 0.7f)
                                                )
                                )
                        }
                ) { padding ->
                        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                                val isDesktop = maxWidth >= 600.dp
                                val contentModifier =
                                        if (isDesktop) {
                                                Modifier.width(500.dp)
                                                        .align(Alignment.Center)
                                                        .background(
                                                                Color(0xFF1E293B),
                                                                RoundedCornerShape(24.dp)
                                                        )
                                                        .padding(32.dp)
                                        } else {
                                                Modifier.fillMaxSize().padding(16.dp)
                                        }

                                Column(
                                        modifier = contentModifier,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        if (isDesktop) {
                                                Text(
                                                        stringResource(Res.string.add_transaction),
                                                        style =
                                                                MaterialTheme.typography
                                                                        .headlineSmall.copy(
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color = Color.White
                                                                ),
                                                        modifier =
                                                                Modifier.padding(bottom = 16.dp)
                                                                        .align(
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                        )
                                                )
                                        }

                                        // Type Segmented Control (Simple implementation using Row
                                        // of Buttons)
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                TypeButton(
                                                        text = stringResource(Res.string.expense),
                                                        selected = type == TransactionType.EXPENSE,
                                                        onClick = {
                                                                type = TransactionType.EXPENSE
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        color = Color(0xFFEF4444) // Red
                                                )
                                                TypeButton(
                                                        text = stringResource(Res.string.income),
                                                        selected = type == TransactionType.INCOME,
                                                        onClick = { type = TransactionType.INCOME },
                                                        modifier = Modifier.weight(1f),
                                                        color = Color(0xFF10B981) // Green
                                                )
                                        }

                                        // Amount
                                        OutlinedTextField(
                                                value = amount,
                                                onValueChange = {
                                                        if (it.all { char ->
                                                                        char.isDigit() ||
                                                                                char == '.'
                                                                }
                                                        )
                                                                amount = it
                                                },
                                                label = { Text(stringResource(Res.string.amount)) },
                                                modifier = Modifier.fillMaxWidth(),
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Number
                                                        ),
                                                colors =
                                                        OutlinedTextFieldDefaults.colors(
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White,
                                                                focusedBorderColor =
                                                                        Color(
                                                                                0xFF7C3AED
                                                                        ), // Violet 600
                                                                unfocusedBorderColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.5f
                                                                        ),
                                                                focusedLabelColor =
                                                                        Color(0xFF7C3AED),
                                                                unfocusedLabelColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.5f
                                                                        )
                                                        )
                                        )

                                        // Category Dropdown
                                        ExposedDropdownMenuBox(
                                                expanded = expanded,
                                                onExpandedChange = { expanded = !expanded },
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                                OutlinedTextField(
                                                        value = category.name,
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = {
                                                                Text(
                                                                        stringResource(
                                                                                Res.string.category
                                                                        )
                                                                )
                                                        },
                                                        trailingIcon = {
                                                                ExposedDropdownMenuDefaults
                                                                        .TrailingIcon(
                                                                                expanded = expanded
                                                                        )
                                                        },
                                                        modifier =
                                                                Modifier.menuAnchor()
                                                                        .fillMaxWidth(),
                                                        colors =
                                                                OutlinedTextFieldDefaults.colors(
                                                                        focusedTextColor =
                                                                                Color.White,
                                                                        unfocusedTextColor =
                                                                                Color.White,
                                                                        focusedBorderColor =
                                                                                Color(0xFF7C3AED),
                                                                        unfocusedBorderColor =
                                                                                Color.White.copy(
                                                                                        alpha = 0.5f
                                                                                ),
                                                                        focusedLabelColor =
                                                                                Color(0xFF7C3AED),
                                                                        unfocusedLabelColor =
                                                                                Color.White.copy(
                                                                                        alpha = 0.5f
                                                                                )
                                                                )
                                                )
                                                ExposedDropdownMenu(
                                                        expanded = expanded,
                                                        onDismissRequest = { expanded = false }
                                                ) {
                                                        PersonalCategory.entries.forEach {
                                                                selectionOption ->
                                                                DropdownMenuItem(
                                                                        text = {
                                                                                Text(
                                                                                        selectionOption
                                                                                                .name
                                                                                )
                                                                        },
                                                                        onClick = {
                                                                                category =
                                                                                        selectionOption
                                                                                expanded = false
                                                                        }
                                                                )
                                                        }
                                                }
                                        }

                                        if (type == TransactionType.INCOME) {
                                                OutlinedTextField(
                                                        value = source,
                                                        onValueChange = { source = it },
                                                        label = {
                                                                Text(
                                                                        stringResource(
                                                                                Res.string
                                                                                        .source_hint
                                                                        )
                                                                )
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors =
                                                                OutlinedTextFieldDefaults.colors(
                                                                        focusedTextColor =
                                                                                Color.White,
                                                                        unfocusedTextColor =
                                                                                Color.White,
                                                                        focusedBorderColor =
                                                                                Color(0xFF7C3AED),
                                                                        unfocusedBorderColor =
                                                                                Color.White.copy(
                                                                                        alpha = 0.5f
                                                                                ),
                                                                        focusedLabelColor =
                                                                                Color(0xFF7C3AED),
                                                                        unfocusedLabelColor =
                                                                                Color.White.copy(
                                                                                        alpha = 0.5f
                                                                                )
                                                                )
                                                )
                                        }

                                        // Note
                                        OutlinedTextField(
                                                value = note,
                                                onValueChange = { note = it },
                                                label = {
                                                        Text(stringResource(Res.string.note_hint))
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors =
                                                        OutlinedTextFieldDefaults.colors(
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White,
                                                                focusedBorderColor =
                                                                        Color(0xFF7C3AED),
                                                                unfocusedBorderColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.5f
                                                                        ),
                                                                focusedLabelColor =
                                                                        Color(0xFF7C3AED),
                                                                unfocusedLabelColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.5f
                                                                        )
                                                        )
                                        )

                                        if (errorMessage != null) {
                                                Text(
                                                        text = errorMessage!!,
                                                        color = MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.bodySmall
                                                )
                                        }

                                        if (!isDesktop) Spacer(modifier = Modifier.weight(1f))

                                        Button(
                                                onClick = {
                                                        val amountValue = amount.toDoubleOrNull()
                                                        if (amountValue == null || amountValue <= 0
                                                        ) {
                                                                errorMessage = invalidAmountMessage
                                                                return@Button
                                                        }

                                                        scope.launch {
                                                                isLoading = true
                                                                errorMessage = null
                                                                val request =
                                                                        CreatePersonalExpenseRequest(
                                                                                amount =
                                                                                        amountValue,
                                                                                category = category,
                                                                                note = note,
                                                                                date =
                                                                                        kotlin.time
                                                                                                .Clock
                                                                                                .System
                                                                                                .now()
                                                                                                .toEpochMilliseconds(),
                                                                                type = type,
                                                                                source =
                                                                                        if (type ==
                                                                                                        TransactionType
                                                                                                                .INCOME
                                                                                        )
                                                                                                source
                                                                                        else null
                                                                        )
                                                                val result =
                                                                        apiClient
                                                                                .createPersonalExpense(
                                                                                        request
                                                                                )
                                                                result.fold(
                                                                        onSuccess = {
                                                                                isLoading = false
                                                                                onNavigateBack()
                                                                        },
                                                                        onFailure = {
                                                                                isLoading = false
                                                                                errorMessage =
                                                                                        it.message
                                                                        }
                                                                )
                                                        }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                                enabled = !isLoading,
                                                shape = RoundedCornerShape(16.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        Color(
                                                                                0xFF7C3AED
                                                                        ) // Violet 600
                                                        )
                                        ) {
                                                if (isLoading) {
                                                        CircularProgressIndicator(
                                                                color = Color.White,
                                                                modifier = Modifier.size(24.dp)
                                                        )
                                                } else {
                                                        Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = null
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                stringResource(
                                                                        Res.string.save_transaction
                                                                )
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun TypeButton(
        text: String,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        color: Color
) {
        val containerColor = if (selected) color else Color(0xFF1E293B)
        val contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.6f)

        Button(
                onClick = onClick,
                modifier = modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor)
        ) {
                Text(
                        text,
                        color = contentColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
        }
}
