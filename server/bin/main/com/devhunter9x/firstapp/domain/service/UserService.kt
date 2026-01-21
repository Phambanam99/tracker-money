package com.devhunter9x.firstapp.domain.service

import User
import com.devhunter9x.firstapp.domain.repository.UserRepository

class UserService(private val userRepository: UserRepository) {

    suspend fun getUsersByRoom(roomId: String): List<User> {
        return userRepository.findByRoomId(roomId)
    }

    suspend fun getUserById(id: String): User? {
        return userRepository.findById(id)
    }

    suspend fun getUserByName(name: String): User? {
        return userRepository.findByName(name)
    }
}
