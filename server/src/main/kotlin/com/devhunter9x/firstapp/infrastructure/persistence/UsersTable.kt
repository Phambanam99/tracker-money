package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 50)
    val name = varchar("name", 100).uniqueIndex("idx_user_name_unique") // Global unique username
    val passwordHash = varchar("password_hash", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}