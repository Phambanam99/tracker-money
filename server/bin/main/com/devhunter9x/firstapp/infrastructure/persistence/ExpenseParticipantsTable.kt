package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ExpenseParticipantsTable : Table("expense_participants") {
    val expenseId =
            varchar("expense_id", 50)
                    .references(ExpensesTable.id, onDelete = ReferenceOption.CASCADE)
    val userId =
            varchar("user_id", 50).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val amount = double("amount") // Số tiền participant phải trả

    override val primaryKey = PrimaryKey(expenseId, userId)
}
