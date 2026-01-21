package com.devhunter9x.firstapp.domain.repository

import User

interface UserRepository {
    suspend fun findByNameAndRoom(name: String, roomId: String): User?
    suspend fun findById(id: String): User?
    suspend fun findByRoomId(roomId: String): List<User>
    suspend fun save(user: User, roomId: String, passwordHash: String?): Boolean
}