package com.devhunter9x.firstapp.domain.service

import User
import com.devhunter9x.firstapp.domain.repository.UserRepository
import com.devhunter9x.firstapp.exception.UserAlreadyExistsException
import java.util.UUID

class UserService(private val userRepository: UserRepository) {

    suspend fun registerUser(name: String, roomId: String, passwordHash: String? = null): User {
        // Kiểm tra Business Logic: User đã tồn tại chưa?
        val existingUser = userRepository.findByNameAndRoom(name, roomId)
        if (existingUser != null) {
            throw UserAlreadyExistsException("Người dùng '$name' đã tồn tại trong phòng")
        }

        // Tạo User mới với ID do Server sinh ra
        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name
        )

        // Lưu vào DB
        userRepository.save(newUser, roomId, passwordHash)

        return newUser
    }

    suspend fun getUsersByRoom(roomId: String): List<User> {
        return userRepository.findByRoomId(roomId)
    }

    suspend fun getUserById(id: String): User? {
        return userRepository.findById(id)
    }
}