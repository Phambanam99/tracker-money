package com.devhunter9x.firstapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devhunter9x.firstapp.*
import com.devhunter9x.firstapp.api.ApiClient
import com.devhunter9x.firstapp.generated.resources.*
import com.devhunter9x.firstapp.util.formatCurrency
import com.devhunter9x.firstapp.util.formatDate
import com.devhunter9x.firstapp.util.formatInputMoney
import com.devhunter9x.firstapp.util.parseMoney
import kotlin.collections.find
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

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
        var splitEqually by remember { mutableStateOf(true) }
        var participantAmounts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        var selectedDate by remember { mutableStateOf("") }
        val todayStr = stringResource(Res.string.today)
        LaunchedEffect(Unit) { if (selectedDate.isEmpty()) selectedDate = todayStr }
        var showDatePicker by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        val failedToAddExpenseMessage = stringResource(Res.string.failed_to_add_expense)
        val mustBeAmountMessage = stringResource(Res.string.total_must_be)

        LaunchedEffect(roomId) {
                apiClient.getMembers(roomId).onSuccess {
                        members = it
                        selectedParticipants = it.map { m: User -> m.id }.toSet()
                        participantAmounts = it.associate { m: User -> m.id to "" }
                }
        }

        val totalParticipantAmounts =
                participantAmounts.filterKeys { selectedParticipants.contains(it) }.values.sumOf {
                        it.parseMoney() ?: 0.0
                }

        val totalAmount = amount.parseMoney() ?: 0.0
        val isAmountValid =
                if (splitEqually) true else abs(totalParticipantAmounts - totalAmount) < 0.01
        val payerName =
                members.find { it.id == selectedPayerId }?.name
                        ?: stringResource(Res.string.not_selected)

        Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                        CenterAlignedTopAppBar(
                                colors =
                                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                containerColor = Color.Transparent
                                        ),
                                title = {
                                        Text(
                                                stringResource(Res.string.add_expense),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold
                                        )
                                },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateBack) {
                                                Icon(
                                                        imageVector =
                                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription =
                                                                stringResource(Res.string.back),
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                        }
                                }
                        )
                }
        ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        // Background gradient
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        Brush.verticalGradient(
                                                                colors =
                                                                        listOf(
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .background,
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.5f
                                                                                        )
                                                                        )
                                                        )
                                                )
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val contentWidth = if (maxWidth > 600.dp) 500.dp else maxWidth

                                Column(
                                        modifier =
                                                Modifier.width(contentWidth)
                                                        .align(Alignment.TopCenter)
                                                        .padding(horizontal = 20.dp)
                                                        .verticalScroll(scrollState)
                                ) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // === SECTION: INFO ===
                                        SectionHeader(
                                                stringResource(Res.string.expense_details),
                                                Icons.Default.Payments
                                        )

                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(24.dp),
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .surface.copy(
                                                                                alpha = 0.6f
                                                                        )
                                                        ),
                                                border =
                                                        BorderStroke(
                                                                1.dp,
                                                                MaterialTheme.colorScheme.outline
                                                                        .copy(alpha = 0.1f)
                                                        )
                                        ) {
                                                Column(modifier = Modifier.padding(20.dp)) {
                                                        OutlinedTextField(
                                                                value = amount,
                                                                onValueChange = {
                                                                        amount =
                                                                                it.formatInputMoney()
                                                                },
                                                                label = {
                                                                        Text(
                                                                                stringResource(
                                                                                        Res.string
                                                                                                .amount
                                                                                )
                                                                        )
                                                                },
                                                                singleLine = true,
                                                                keyboardOptions =
                                                                        KeyboardOptions(
                                                                                keyboardType =
                                                                                        KeyboardType
                                                                                                .Number
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(16.dp),
                                                                leadingIcon = {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .AttachMoney,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                        )
                                                                }
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        OutlinedTextField(
                                                                value = description,
                                                                onValueChange = {
                                                                        description = it
                                                                },
                                                                label = {
                                                                        Text(
                                                                                stringResource(
                                                                                        Res.string
                                                                                                .description_hint
                                                                                )
                                                                        )
                                                                },
                                                                singleLine = true,
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(16.dp),
                                                                leadingIcon = {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .Description,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                        )
                                                                }
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        OutlinedTextField(
                                                                value = selectedDate,
                                                                onValueChange = {},
                                                                label = {
                                                                        Text(
                                                                                stringResource(
                                                                                        Res.string
                                                                                                .date
                                                                                )
                                                                        )
                                                                },
                                                                singleLine = true,
                                                                readOnly = true,
                                                                modifier = Modifier.fillMaxWidth(),
                                                                shape = RoundedCornerShape(16.dp),
                                                                leadingIcon = {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .CalendarMonth,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                        )
                                                                },
                                                                trailingIcon = {
                                                                        TextButton(
                                                                                onClick = {
                                                                                        showDatePicker =
                                                                                                true
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        stringResource(
                                                                                                Res.string
                                                                                                        .select
                                                                                        ),
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                )
                                                                        }
                                                                }
                                                        )
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // === SECTION: SPLIT ===
                                        SectionHeader(
                                                stringResource(Res.string.split_strategy),
                                                Icons.Default.Balance
                                        )

                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                SplitChip(
                                                        selected = splitEqually,
                                                        onClick = { splitEqually = true },
                                                        label = stringResource(Res.string.equal),
                                                        modifier = Modifier.weight(1f)
                                                )
                                                SplitChip(
                                                        selected = !splitEqually,
                                                        onClick = { splitEqually = false },
                                                        label = stringResource(Res.string.manual),
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // === SECTION: PAYER ===
                                        SectionHeader(
                                                stringResource(Res.string.payer),
                                                Icons.Default.Person
                                        )

                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(24.dp),
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .surface.copy(
                                                                                alpha = 0.6f
                                                                        )
                                                        ),
                                                border =
                                                        BorderStroke(
                                                                1.dp,
                                                                MaterialTheme.colorScheme.outline
                                                                        .copy(alpha = 0.1f)
                                                        )
                                        ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                        members.forEach { member ->
                                                                val isSelected =
                                                                        selectedPayerId == member.id
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .clip(
                                                                                                RoundedCornerShape(
                                                                                                        16.dp
                                                                                                )
                                                                                        )
                                                                                        .background(
                                                                                                if (isSelected
                                                                                                )
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .primary
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.1f
                                                                                                                )
                                                                                                else
                                                                                                        Color.Transparent
                                                                                        )
                                                                                        .padding(
                                                                                                horizontal =
                                                                                                        8.dp,
                                                                                                vertical =
                                                                                                        4.dp
                                                                                        ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        RadioButton(
                                                                                selected =
                                                                                        isSelected,
                                                                                onClick = {
                                                                                        selectedPayerId =
                                                                                                member.id
                                                                                },
                                                                                colors =
                                                                                        RadioButtonDefaults
                                                                                                .colors(
                                                                                                        selectedColor =
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .primary
                                                                                                )
                                                                        )
                                                                        Text(
                                                                                member.name,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodyLarge,
                                                                                fontWeight =
                                                                                        if (isSelected
                                                                                        )
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                        else
                                                                                                FontWeight
                                                                                                        .Normal,
                                                                                color =
                                                                                        if (isSelected
                                                                                        )
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary
                                                                                        else
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurface
                                                                        )
                                                                }
                                                        }
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // === SECTION: PARTICIPANTS ===
                                        SectionHeader(
                                                stringResource(Res.string.participants),
                                                Icons.Default.Groups
                                        )

                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(24.dp),
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .surface.copy(
                                                                                alpha = 0.6f
                                                                        )
                                                        ),
                                                border =
                                                        BorderStroke(
                                                                1.dp,
                                                                MaterialTheme.colorScheme.outline
                                                                        .copy(alpha = 0.1f)
                                                        )
                                        ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                        members.forEach { member ->
                                                                val isSelected =
                                                                        selectedParticipants
                                                                                .contains(member.id)
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                vertical =
                                                                                                        2.dp
                                                                                        ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Checkbox(
                                                                                checked =
                                                                                        isSelected,
                                                                                onCheckedChange = {
                                                                                        checked ->
                                                                                        selectedParticipants =
                                                                                                if (checked
                                                                                                )
                                                                                                        selectedParticipants +
                                                                                                                member.id
                                                                                                else
                                                                                                        selectedParticipants -
                                                                                                                member.id
                                                                                }
                                                                        )

                                                                        if (splitEqually) {
                                                                                Text(
                                                                                        member.name,
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                        1f
                                                                                                )
                                                                                )
                                                                                if (isSelected &&
                                                                                                amount.isNotEmpty()
                                                                                ) {
                                                                                        val perPerson =
                                                                                                totalAmount /
                                                                                                        selectedParticipants
                                                                                                                .size
                                                                                        Text(
                                                                                                "${perPerson.formatCurrency()}đ",
                                                                                                color =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .primary,
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold
                                                                                        )
                                                                                }
                                                                        } else {
                                                                                Text(
                                                                                        member.name,
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                        0.4f
                                                                                                )
                                                                                )
                                                                                if (isSelected) {
                                                                                        OutlinedTextField(
                                                                                                value =
                                                                                                        participantAmounts[
                                                                                                                member.id]
                                                                                                                ?: "",
                                                                                                onValueChange = {
                                                                                                        value
                                                                                                        ->
                                                                                                        participantAmounts =
                                                                                                                participantAmounts +
                                                                                                                        (member.id to
                                                                                                                                value.formatInputMoney())
                                                                                                },
                                                                                                modifier =
                                                                                                        Modifier.weight(
                                                                                                                0.6f
                                                                                                        ),
                                                                                                shape =
                                                                                                        RoundedCornerShape(
                                                                                                                12.dp
                                                                                                        ),
                                                                                                singleLine =
                                                                                                        true,
                                                                                                keyboardOptions =
                                                                                                        KeyboardOptions(
                                                                                                                keyboardType =
                                                                                                                        KeyboardType
                                                                                                                                .Number
                                                                                                        ),
                                                                                                suffix = {
                                                                                                        Text(
                                                                                                                stringResource(
                                                                                                                        Res.string
                                                                                                                                .currency_symbol
                                                                                                                )
                                                                                                        )
                                                                                                },
                                                                                                isError =
                                                                                                        !isAmountValid
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // === SUMMARY ===
                                        if (amount.isNotEmpty() && selectedParticipants.isNotEmpty()
                                        ) {
                                                SectionHeader(
                                                        stringResource(Res.string.summary),
                                                        Icons.Default.Summarize
                                                )
                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(24.dp),
                                                        colors =
                                                                CardDefaults.cardColors(
                                                                        containerColor =
                                                                                if (isAmountValid)
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primaryContainer
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.2f
                                                                                                )
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .errorContainer
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.2f
                                                                                                )
                                                                ),
                                                        border =
                                                                BorderStroke(
                                                                        1.dp,
                                                                        if (isAmountValid)
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.2f
                                                                                        )
                                                                        else
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .error.copy(
                                                                                        alpha = 0.2f
                                                                                )
                                                                )
                                                ) {
                                                        Column(modifier = Modifier.padding(20.dp)) {
                                                                SummaryRow(
                                                                        stringResource(
                                                                                Res.string
                                                                                        .total_expense
                                                                        ),
                                                                        "${totalAmount.formatCurrency()}${stringResource(Res.string.currency_symbol)}"
                                                                )
                                                                SummaryRow(
                                                                        stringResource(
                                                                                Res.string.payer
                                                                        ),
                                                                        payerName
                                                                )
                                                                SummaryRow(
                                                                        stringResource(
                                                                                Res.string
                                                                                        .participants
                                                                        ),
                                                                        "${selectedParticipants.size}"
                                                                )

                                                                if (!splitEqually) {
                                                                        SummaryRow(
                                                                                stringResource(
                                                                                        Res.string
                                                                                                .entered_total
                                                                                ),
                                                                                "${totalParticipantAmounts.formatCurrency()}${stringResource(Res.string.currency_symbol)}"
                                                                        )
                                                                        if (!isAmountValid) {
                                                                                Text(
                                                                                        "⚠️ $mustBeAmountMessage ${totalAmount.formatCurrency()}${stringResource(Res.string.currency_symbol)}",
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .error,
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodySmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        top =
                                                                                                                8.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(32.dp))

                                        errorMessage?.let { error ->
                                                Text(
                                                        text = error,
                                                        color = MaterialTheme.colorScheme.error,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        Button(
                                                onClick = {
                                                        val amountValue = amount.parseMoney() ?: 0.0
                                                        val finalAmounts =
                                                                if (splitEqually) {
                                                                        val perPerson =
                                                                                amountValue /
                                                                                        selectedParticipants
                                                                                                .size
                                                                        selectedParticipants
                                                                                .associateWith {
                                                                                        perPerson
                                                                                }
                                                                } else {
                                                                        selectedParticipants
                                                                                .associateWith {
                                                                                        participantAmounts[
                                                                                                        it]
                                                                                                ?.toDoubleOrNull()
                                                                                                ?: 0.0
                                                                                }
                                                                }

                                                        scope.launch {
                                                                isLoading = true
                                                                errorMessage = null
                                                                apiClient
                                                                        .createExpense(
                                                                                roomId = roomId,
                                                                                payerId =
                                                                                        selectedPayerId,
                                                                                amount =
                                                                                        amountValue,
                                                                                description =
                                                                                        description,
                                                                                participantAmounts =
                                                                                        finalAmounts,
                                                                                splitEqually =
                                                                                        splitEqually
                                                                        )
                                                                        .fold(
                                                                                onSuccess = {
                                                                                        onExpenseAdded()
                                                                                },
                                                                                onFailure = {
                                                                                        e: Throwable
                                                                                        ->
                                                                                        errorMessage =
                                                                                                e.message
                                                                                                        ?: failedToAddExpenseMessage
                                                                                }
                                                                        )
                                                                isLoading = false
                                                        }
                                                },
                                                enabled =
                                                        amount.isNotBlank() &&
                                                                description.isNotBlank() &&
                                                                selectedParticipants.isNotEmpty() &&
                                                                !isLoading &&
                                                                isAmountValid,
                                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                                shape = RoundedCornerShape(20.dp),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        ),
                                                elevation = ButtonDefaults.buttonElevation(8.dp)
                                        ) {
                                                if (isLoading) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary
                                                        )
                                                } else {
                                                        Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = null
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text(
                                                                stringResource(
                                                                        Res.string.confirm_expense
                                                                ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(60.dp))
                                }
                        }
                }
        }

        if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                        selectedDate = millis.formatDate()
                                                }
                                                showDatePicker = false
                                        }
                                ) {
                                        Text(
                                                stringResource(Res.string.select),
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        },
                        dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                        Text(stringResource(Res.string.cancel))
                                }
                        }
                ) {
                        Column {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        FilterChip(
                                                selected = false,
                                                onClick = {
                                                        selectedDate = "Today"
                                                        showDatePicker = false
                                                },
                                                label = { Text(stringResource(Res.string.today)) }
                                        )
                                        FilterChip(
                                                selected = false,
                                                onClick = {
                                                        selectedDate = "Yesterday"
                                                        showDatePicker = false
                                                },
                                                label = {
                                                        Text(stringResource(Res.string.yesterday))
                                                }
                                        )
                                }
                                DatePicker(state = datePickerState)
                        }
                }
        }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
                Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                )
        }
}

@Composable
private fun SplitChip(
        selected: Boolean,
        onClick: () -> Unit,
        label: String,
        modifier: Modifier = Modifier
) {
        Surface(
                onClick = onClick,
                modifier = modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp),
                color =
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                border =
                        BorderStroke(
                                1.dp,
                                if (selected) Color.Transparent
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
        ) {
                Box(contentAlignment = Alignment.Center) {
                        Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color =
                                        if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

@Composable
private fun SummaryRow(label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
                Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                )
        }
}
