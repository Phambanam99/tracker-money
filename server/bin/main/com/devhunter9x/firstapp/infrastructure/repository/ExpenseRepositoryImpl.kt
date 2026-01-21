package com.devhunter9x.firstapp.infrastructure.repository

import Expense
import com.devhunter9x.firstapp.domain.repository.ExpenseRepository
import com.devhunter9x.firstapp.infrastructure.persistence.ExpenseParticipantsTable
import com.devhunter9x.firstapp.infrastructure.persistence.ExpensesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ExpenseRepositoryImpl : ExpenseRepository {
    
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun findByRoomId(roomId: String): List<Expense> = dbQuery {
        ExpensesTable.selectAll()
            .where { ExpensesTable.roomId eq roomId }
            .orderBy(ExpensesTable.createdAt, SortOrder.DESC)
            .map { row ->
                val expenseId = row[ExpensesTable.id]
                val participantIds = getParticipantIdsSync(expenseId)
                row.toExpense(participantIds)
            }
    }

    override suspend fun findById(id: String): Expense? = dbQuery {
        ExpensesTable.selectAll()
            .where { ExpensesTable.id eq id }
            .map { row ->
                val participantIds = getParticipantIdsSync(row[ExpensesTable.id])
                row.toExpense(participantIds)
            }
            .singleOrNull()
    }

    override suspend fun create(expense: Expense): Boolean = dbQuery {
        ExpensesTable.insert {
            it[id] = expense.id
            it[roomId] = expense.roomId
            it[payerId] = expense.payerId
            it[amount] = expense.amount.toBigDecimal()
            it[description] = expense.description
        }.insertedCount > 0
    }

    override suspend fun addParticipants(expenseId: String, userIds: List<String>): Boolean = dbQuery {
        userIds.forEach { userId ->
            ExpenseParticipantsTable.insert {
                it[ExpenseParticipantsTable.expenseId] = expenseId
                it[ExpenseParticipantsTable.userId] = userId
            }
        }
        true
    }

    override suspend fun getParticipantIds(expenseId: String): List<String> = dbQuery {
        getParticipantIdsSync(expenseId)
    }

    private fun getParticipantIdsSync(expenseId: String): List<String> {
        return ExpenseParticipantsTable.selectAll()
            .where { ExpenseParticipantsTable.expenseId eq expenseId }
            .map { it[ExpenseParticipantsTable.userId] }
    }

    private fun ResultRow.toExpense(participantIds: List<String>) = Expense(
        id = this[ExpensesTable.id],
        roomId = this[ExpensesTable.roomId],
        payerId = this[ExpensesTable.payerId],
        amount = this[ExpensesTable.amount].toDouble(),
        description = this[ExpensesTable.description],
        participantIds = participantIds,
        timestamp = this[ExpensesTable.createdAt].toString()
    )
}
