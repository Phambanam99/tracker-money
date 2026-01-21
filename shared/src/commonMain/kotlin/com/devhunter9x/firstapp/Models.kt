package com.devhunter9x.firstapp

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
        val id: String,
        val roomId: String,
        val payerId: String,
        val amount: Double,
        val description: String,
        val participantAmounts:
                Map<String, Double>, // userId -> amount (số tiền mỗi người phải trả)
        val timestamp: String
)

@Serializable data class User(val id: String, val name: String)

@Serializable data class Room(val id: String, val code: String, val name: String)

@Serializable data class Balance(val fromUser: User, val toUser: User, val amount: Double)

// ===== Request DTOs =====

@Serializable data class CreateRoomRequest(val name: String, val code: String)

@Serializable data class JoinRoomRequest(val roomCode: String)

@Serializable data class LoginRequest(val name: String, val password: String)

@Serializable data class RegisterRequest(val name: String, val password: String)

@Serializable
data class CreateExpenseRequest(
        val payerId: String,
        val amount: Double,
        val description: String,
        val participantAmounts: Map<String, Double>, // userId -> amount
        val splitEqually: Boolean = true // true = chia đều, false = dùng participantAmounts
)

// ===== Response DTOs =====

@Serializable data class AuthResponse(val token: String, val user: User)

@Serializable data class JoinRoomResponse(val room: Room, val token: String)

@Serializable data class RoomListResponse(val rooms: List<Room>)

@Serializable data class ErrorResponse(val message: String)
