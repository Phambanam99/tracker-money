package com.devhunter9x.firstapp.domain.repository

import Expense

interface ExpenseRepository {
    suspend fun findByRoomId(roomId: String): List<Expense>
    suspend fun findById(id: String): Expense?
    suspend fun create(expense: Expense): Boolean
    suspend fun addParticipants(expenseId: String, userIds: List<String>): Boolean
    suspend fun getParticipantIds(expenseId: String): List<String>
}
