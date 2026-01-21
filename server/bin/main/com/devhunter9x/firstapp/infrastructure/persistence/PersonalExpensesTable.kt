package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PersonalExpensesTable : Table("personal_expenses") {
    val id = varchar("id", 50)
    val userId =
            varchar("user_id", 50).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val amount = decimal("amount", 15, 2)
    val category = varchar("category", 50) // Store enum as string
    val note = text("note")
    val date = long("date") // Timestamp
    val type = varchar("type", 20) // INCOME/EXPENSE
    val source_ = varchar("source", 100).nullable() // Nguồn thu

    override val primaryKey = PrimaryKey(id)
}
