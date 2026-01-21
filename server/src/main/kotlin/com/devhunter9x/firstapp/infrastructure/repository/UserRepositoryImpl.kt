package com.devhunter9x.firstapp.infrastructure.repository

import User
import com.devhunter9x.firstapp.domain.repository.UserRepository
import com.devhunter9x.firstapp.infrastructure.persistence.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl : UserRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun findByNameAndRoom(name: String, roomId: String): User? = dbQuery {
        UsersTable.selectAll()
            .where { (UsersTable.name eq name) and (UsersTable.roomId eq roomId) }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findById(id: String): User? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findByRoomId(roomId: String): List<User> = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.roomId eq roomId }
            .map { it.toUser() }
    }

    override suspend fun save(user: User, roomId: String, passwordHash: String?): Boolean = dbQuery {
        UsersTable.insert {
            it[id] = user.id
            it[name] = user.name
            it[UsersTable.roomId] = roomId
            it[UsersTable.passwordHash] = passwordHash
        }.insertedCount > 0
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        name = this[UsersTable.name]
    )
}