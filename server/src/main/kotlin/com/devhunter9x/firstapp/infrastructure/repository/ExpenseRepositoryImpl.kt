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
                    val participantAmounts = getParticipantAmountsSync(expenseId)
                    row.toExpense(participantAmounts)
                }
    }

    override suspend fun findById(id: String): Expense? = dbQuery {
        ExpensesTable.selectAll()
                .where { ExpensesTable.id eq id }
                .map { row ->
                    val participantAmounts = getParticipantAmountsSync(row[ExpensesTable.id])
                    row.toExpense(participantAmounts)
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
                }
                .insertedCount > 0
    }

    override suspend fun addParticipants(
            expenseId: String,
            participantAmounts: Map<String, Double>
    ): Boolean = dbQuery {
        participantAmounts.forEach { (userId, amount) ->
            ExpenseParticipantsTable.insert {
                it[ExpenseParticipantsTable.expenseId] = expenseId
                it[ExpenseParticipantsTable.userId] = userId
                it[ExpenseParticipantsTable.amount] = amount
            }
        }
        true
    }

    override suspend fun getParticipantAmounts(expenseId: String): Map<String, Double> = dbQuery {
        getParticipantAmountsSync(expenseId)
    }

    private fun getParticipantAmountsSync(expenseId: String): Map<String, Double> {
        return ExpenseParticipantsTable.selectAll()
                .where { ExpenseParticipantsTable.expenseId eq expenseId }
                .associate {
                    it[ExpenseParticipantsTable.userId] to it[ExpenseParticipantsTable.amount]
                }
    }

    private fun ResultRow.toExpense(participantAmounts: Map<String, Double>) =
            Expense(
                    id = this[ExpensesTable.id],
                    roomId = this[ExpensesTable.roomId],
                    payerId = this[ExpensesTable.payerId],
                    amount = this[ExpensesTable.amount].toDouble(),
                    description = this[ExpensesTable.description],
                    participantAmounts = participantAmounts,
                    timestamp = this[ExpensesTable.createdAt].toString()
            )
}
