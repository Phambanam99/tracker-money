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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonalExpenseScreen(apiClient: ApiClient, onNavigateBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(PersonalCategory.OTHER) }
    var expanded by remember { mutableStateOf(false) } // For category dropdown
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
                                                            Color(0xFF1E293B).copy(alpha = 0.5f),
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
                                        "Add Transaction",
                                        style =
                                                MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                )
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                            Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                    )
                                }
                            },
                            colors =
                                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
                                    )
                    )
                }
        ) { padding ->
            Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type Segmented Control (Simple implementation using Row of Buttons)
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeButton(
                            text = "Expense",
                            selected = type == TransactionType.EXPENSE,
                            onClick = { type = TransactionType.EXPENSE },
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFEF4444) // Red
                    )
                    TypeButton(
                            text = "Income",
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
                            if (it.all { char -> char.isDigit() || char == '.' }) amount = it
                        },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
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
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                    )
                    )
                    ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                    ) {
                        PersonalCategory.entries.forEach { selectionOption ->
                            DropdownMenuItem(
                                    text = { Text(selectionOption.name) },
                                    onClick = {
                                        category = selectionOption
                                        expanded = false
                                    }
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                                )
                )

                if (errorMessage != null) {
                    Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val request =
                                        CreatePersonalExpenseRequest(
                                                amount = amountValue,
                                                category = category,
                                                note = note,
                                                date = Clock.System.now().toEpochMilliseconds(),
                                                type = type
                                        )
                                val result = apiClient.createPersonalExpense(request)
                                result.fold(
                                        onSuccess = {
                                            isLoading = false
                                            onNavigateBack()
                                        },
                                        onFailure = {
                                            isLoading = false
                                            errorMessage = it.message
                                        }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Transaction")
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
