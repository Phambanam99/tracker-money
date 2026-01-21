package com.devhunter9x.firstapp.infrastructure.repository

import com.devhunter9x.firstapp.PersonalCategory
import com.devhunter9x.firstapp.PersonalExpense
import com.devhunter9x.firstapp.TransactionType
import com.devhunter9x.firstapp.domain.repository.PersonalExpenseRepository
import com.devhunter9x.firstapp.infrastructure.persistence.PersonalExpensesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PersonalExpenseRepositoryImpl : PersonalExpenseRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
            newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun findByUserId(userId: String): List<PersonalExpense> = dbQuery {
        PersonalExpensesTable.selectAll()
                .where { PersonalExpensesTable.userId eq userId }
                .orderBy(PersonalExpensesTable.date, SortOrder.DESC)
                .map { row -> row.toPersonalExpense() }
    }

    override suspend fun create(userId: String, expense: PersonalExpense): Boolean = dbQuery {
        PersonalExpensesTable.insert {
                    it[id] = expense.id
                    it[this.userId] = userId
                    it[amount] = expense.amount.toBigDecimal()
                    it[category] = expense.category.name
                    it[note] = expense.note
                    it[date] = expense.date
                    it[type] = expense.type.name
                    it[source_] = expense.source
                }
                .insertedCount > 0
    }

    override suspend fun delete(expenseId: String): Boolean = dbQuery {
        PersonalExpensesTable.deleteWhere { PersonalExpensesTable.id eq expenseId } > 0
    }

    private fun ResultRow.toPersonalExpense() =
            PersonalExpense(
                    id = this[PersonalExpensesTable.id],
                    amount = this[PersonalExpensesTable.amount].toDouble(),
                    category = PersonalCategory.valueOf(this[PersonalExpensesTable.category]),
                    note = this[PersonalExpensesTable.note],
                    date = this[PersonalExpensesTable.date],
                    type = TransactionType.valueOf(this[PersonalExpensesTable.type]),
                    source = this[PersonalExpensesTable.source_]
            )
}
