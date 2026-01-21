package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ExpensesTable : Table("expenses") {
    val id = varchar("id", 50)
    val roomId = varchar("room_id", 50).references(RoomsTable.id, onDelete = ReferenceOption.CASCADE)
    val payerId = varchar("payer_id", 50).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val amount = decimal("amount", 15, 2)
    val description = text("description")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
