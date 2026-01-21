package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object RoomsTable : Table("rooms") {
    val id = varchar("id", 50)
    val code = varchar("code", 20).uniqueIndex()
    val name = varchar("name", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
