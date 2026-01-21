package com.devhunter9x.firstapp.domain.service

import User
import com.devhunter9x.firstapp.config.JwtConfig
import com.devhunter9x.firstapp.domain.repository.RoomRepository
import com.devhunter9x.firstapp.domain.repository.UserRepository
import com.devhunter9x.firstapp.exception.InvalidCredentialsException
import com.devhunter9x.firstapp.exception.RoomNotFoundException
import com.devhunter9x.firstapp.exception.UserAlreadyExistsException
import java.security.MessageDigest
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) {

    /**
     * Đăng ký user mới và join vào phòng
     * @return JWT token
     */
    suspend fun register(name: String, roomCode: String, password: String): AuthResult {
        // Tìm phòng theo mã
        val room = roomRepository.findByCode(roomCode)
            ?: throw RoomNotFoundException("Không tìm thấy phòng với mã '$roomCode'")

        // Kiểm tra user đã tồn tại trong phòng chưa
        val existingUser = userRepository.findByNameAndRoom(name, room.id)
        if (existingUser != null) {
            throw UserAlreadyExistsException("Người dùng '$name' đã tồn tại trong phòng '$roomCode'")
        }

        // Tạo user mới
        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name
        )

        val passwordHash = hashPassword(password)
        userRepository.save(newUser, room.id, passwordHash)

        // Tạo JWT token
        val token = JwtConfig.generateToken(newUser.id, newUser.name, room.id)

        return AuthResult(
            token = token,
            user = newUser,
            roomId = room.id,
            roomCode = room.code
        )
    }

    /**
     * Đăng nhập với tên và mật khẩu
     * @return JWT token
     */
    suspend fun login(name: String, roomCode: String, password: String): AuthResult {
        val room = roomRepository.findByCode(roomCode)
            ?: throw RoomNotFoundException("Không tìm thấy phòng với mã '$roomCode'")

        val user = userRepository.findByNameAndRoom(name, room.id)
            ?: throw InvalidCredentialsException("Tên hoặc mật khẩu không đúng")

        // TODO: Verify password (cần thêm field passwordHash vào UserRepository)
        // Hiện tại tạm thời bỏ qua verify password
        
        val token = JwtConfig.generateToken(user.id, user.name, room.id)

        return AuthResult(
            token = token,
            user = user,
            roomId = room.id,
            roomCode = room.code
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

data class AuthResult(
    val token: String,
    val user: User,
    val roomId: String,
    val roomCode: String
)
