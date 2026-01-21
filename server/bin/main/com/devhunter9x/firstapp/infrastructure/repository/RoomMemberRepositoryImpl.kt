package com.devhunter9x.firstapp.infrastructure.repository

import User
import com.devhunter9x.firstapp.domain.repository.RoomMemberRepository
import com.devhunter9x.firstapp.infrastructure.persistence.RoomMembersTable
import com.devhunter9x.firstapp.infrastructure.persistence.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class RoomMemberRepositoryImpl : RoomMemberRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun addMemberToRoom(userId: String, roomId: String): Boolean = dbQuery {
        RoomMembersTable.insert {
            it[RoomMembersTable.userId] = userId
            it[RoomMembersTable.roomId] = roomId
        }.insertedCount > 0
    }

    override suspend fun removeMemberFromRoom(userId: String, roomId: String): Boolean = dbQuery {
        RoomMembersTable.deleteWhere { 
            (RoomMembersTable.userId eq userId) and (RoomMembersTable.roomId eq roomId) 
        } > 0
    }

    override suspend fun getMembersByRoomId(roomId: String): List<User> = dbQuery {
        (RoomMembersTable innerJoin UsersTable)
            .selectAll()
            .where { RoomMembersTable.roomId eq roomId }
            .map { 
                User(
                    id = it[UsersTable.id],
                    name = it[UsersTable.name]
                )
            }
    }

    override suspend fun getRoomIdsByUserId(userId: String): List<String> = dbQuery {
        RoomMembersTable.selectAll()
            .where { RoomMembersTable.userId eq userId }
            .map { it[RoomMembersTable.roomId] }
    }

    override suspend fun isMember(userId: String, roomId: String): Boolean = dbQuery {
        RoomMembersTable.selectAll()
            .where { (RoomMembersTable.userId eq userId) and (RoomMembersTable.roomId eq roomId) }
            .count() > 0
    }
}
