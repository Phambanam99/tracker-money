package com.devhunter9x.firstapp.infrastructure.persistence

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Bảng liên kết User - Room (many-to-many)
 * Cho phép 1 user thuộc nhiều phòng
 */
object RoomMembersTable : Table("room_members") {
    val userId = varchar("user_id", 50).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val roomId = varchar("room_id", 50).references(RoomsTable.id, onDelete = ReferenceOption.CASCADE)
    val joinedAt = datetime("joined_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(userId, roomId)
}
