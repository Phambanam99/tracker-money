package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 50)
    val name = varchar("name", 100)
    val roomId = varchar("room_id", 50).references(RoomsTable.id, onDelete = ReferenceOption.CASCADE)
    val passwordHash = varchar("password_hash", 255).nullable() // Hash của mật khẩu

    override val primaryKey = PrimaryKey(id)

    // Index đảm bảo Tên + Phòng là duy nhất
    val nameRoomIndex = uniqueIndex("idx_user_name_room", name, roomId)
}