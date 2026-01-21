package com.devhunter9x.firstapp.infrastructure.repository

import com.devhunter9x.firstapp.User
import com.devhunter9x.firstapp.domain.repository.UserRepository
import com.devhunter9x.firstapp.infrastructure.persistence.RoomMembersTable
import com.devhunter9x.firstapp.infrastructure.persistence.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl : UserRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
            newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun findByName(name: String): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.name eq name }.map { it.toUser() }.singleOrNull()
    }

    override suspend fun findById(id: String): User? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq id }.map { it.toUser() }.singleOrNull()
    }

    override suspend fun getPasswordHash(userId: String): String? = dbQuery {
        UsersTable.selectAll()
                .where { UsersTable.id eq userId }
                .map { it[UsersTable.passwordHash] }
                .singleOrNull()
    }

    override suspend fun save(user: User, passwordHash: String?): Boolean = dbQuery {
        UsersTable.insert {
                    it[id] = user.id
                    it[name] = user.name
                    it[UsersTable.passwordHash] = passwordHash
                }
                .insertedCount > 0
    }

    /** Lấy danh sách users trong một room (qua RoomMembersTable) */
    override suspend fun findByRoomId(roomId: String): List<User> = dbQuery {
        (RoomMembersTable innerJoin UsersTable)
                .selectAll()
                .where { RoomMembersTable.roomId eq roomId }
                .map { User(id = it[UsersTable.id], name = it[UsersTable.name]) }
    }

    private fun ResultRow.toUser() = User(id = this[UsersTable.id], name = this[UsersTable.name])
}
