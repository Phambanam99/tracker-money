package com.devhunter9x.firstapp

import kotlinx.serialization.Serializable

@Serializable
data class PersonalExpense(
        val id: String,
        val amount: Double,
        val category: PersonalCategory,
        val note: String,
        val date: Long, // Timestamp
        val type: TransactionType
)

@Serializable
enum class PersonalCategory {
    FOOD,
    TRANSPORT,
    UTILITIES,
    SALARY,
    ENTERTAINMENT,
    SHOPPING,
    HEALTH,
    EDUCATION,
    OTHER
}

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE
}

@Serializable
data class CreatePersonalExpenseRequest(
        val amount: Double,
        val category: PersonalCategory,
        val note: String,
        val date: Long,
        val type: TransactionType
)
