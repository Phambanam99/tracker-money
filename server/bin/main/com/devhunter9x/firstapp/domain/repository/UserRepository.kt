package com.devhunter9x.firstapp.domain.repository

import com.devhunter9x.firstapp.User

interface UserRepository {
    suspend fun findByName(name: String): User?
    suspend fun findById(id: String): User?
    suspend fun getPasswordHash(userId: String): String?
    suspend fun save(user: User, passwordHash: String?): Boolean
    suspend fun findByRoomId(roomId: String): List<User>
}
