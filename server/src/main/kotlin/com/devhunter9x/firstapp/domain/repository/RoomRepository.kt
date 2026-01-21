package com.devhunter9x.firstapp.domain.repository

import com.devhunter9x.firstapp.Room

interface RoomRepository {
    suspend fun findByCode(code: String): Room?
    suspend fun findById(id: String): Room?
    suspend fun create(room: Room): Boolean
    suspend fun getAll(): List<Room>
}
