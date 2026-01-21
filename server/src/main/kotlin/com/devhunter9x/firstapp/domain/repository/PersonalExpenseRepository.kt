package com.devhunter9x.firstapp.domain.repository

import com.devhunter9x.firstapp.PersonalExpense

interface PersonalExpenseRepository {
    suspend fun findByUserId(userId: String): List<PersonalExpense>
    suspend fun create(userId: String, expense: PersonalExpense): Boolean
    suspend fun delete(expenseId: String): Boolean
}
