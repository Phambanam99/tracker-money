package com.devhunter9x.firstapp.ui

import kotlinx.serialization.Serializable

// Navigation Routes
@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object CreateRoomRoute

@Serializable
data class RoomDetailRoute(val roomId: String, val roomCode: String)

@Serializable
data class AddExpenseRoute(val roomId: String)

@Serializable
data class BalanceRoute(val roomId: String)
