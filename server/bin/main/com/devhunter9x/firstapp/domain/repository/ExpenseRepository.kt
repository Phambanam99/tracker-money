package com.devhunter9x.firstapp.domain.repository

import Expense

interface ExpenseRepository {
    suspend fun findByRoomId(roomId: String): List<Expense>
    suspend fun findById(id: String): Expense?
    suspend fun create(expense: Expense): Boolean
    suspend fun addParticipants(expenseId: String, participantAmounts: Map<String, Double>): Boolean
    suspend fun getParticipantAmounts(expenseId: String): Map<String, Double>
}
