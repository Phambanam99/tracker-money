package com.devhunter9x.firstapp.domain.service

import User
import com.devhunter9x.firstapp.config.JwtConfig
import com.devhunter9x.firstapp.domain.repository.UserRepository
import com.devhunter9x.firstapp.exception.InvalidCredentialsException
import com.devhunter9x.firstapp.exception.UserAlreadyExistsException
import java.security.MessageDigest
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository
) {

    /**
     * Đăng ký user mới (không cần room)
     */
    suspend fun register(name: String, password: String): AuthResult {
        // Kiểm tra user đã tồn tại chưa
        val existingUser = userRepository.findByName(name)
        if (existingUser != null) {
            throw UserAlreadyExistsException("Tên người dùng '$name' đã tồn tại")
        }

        // Tạo user mới
        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name
        )

        val passwordHash = hashPassword(password)
        userRepository.save(newUser, passwordHash)

        // Tạo JWT token (không có roomId)
        val token = JwtConfig.generateToken(newUser.id, newUser.name)

        return AuthResult(
            token = token,
            user = newUser
        )
    }

    /**
     * Đăng nhập với tên và mật khẩu
     */
    suspend fun login(name: String, password: String): AuthResult {
        val user = userRepository.findByName(name)
            ?: throw InvalidCredentialsException("Tên hoặc mật khẩu không đúng")

        // Verify password
        val storedHash = userRepository.getPasswordHash(user.id)
        if (storedHash == null || storedHash != hashPassword(password)) {
            throw InvalidCredentialsException("Tên hoặc mật khẩu không đúng")
        }
        
        val token = JwtConfig.generateToken(user.id, user.name)

        return AuthResult(
            token = token,
            user = user
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

data class AuthResult(
    val token: String,
    val user: User
)
