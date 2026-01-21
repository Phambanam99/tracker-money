package com.devhunter9x.firstapp.infrastructure.repository

import Room
import com.devhunter9x.firstapp.domain.repository.RoomRepository
import com.devhunter9x.firstapp.infrastructure.persistence.RoomsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class RoomRepositoryImpl : RoomRepository {
    
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun findByCode(code: String): Room? = dbQuery {
        RoomsTable.selectAll()
            .where { RoomsTable.code eq code }
            .map { it.toRoom() }
            .singleOrNull()
    }

    override suspend fun findById(id: String): Room? = dbQuery {
        RoomsTable.selectAll()
            .where { RoomsTable.id eq id }
            .map { it.toRoom() }
            .singleOrNull()
    }

    override suspend fun create(room: Room): Boolean = dbQuery {
        RoomsTable.insert {
            it[id] = room.id
            it[code] = room.code
            it[name] = room.name
        }.insertedCount > 0
    }

    override suspend fun getAll(): List<Room> = dbQuery {
        RoomsTable.selectAll()
            .map { it.toRoom() }
    }

    private fun ResultRow.toRoom() = Room(
        id = this[RoomsTable.id],
        code = this[RoomsTable.code],
        name = this[RoomsTable.name]
    )
}
