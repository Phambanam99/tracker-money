package com.devhunter9x.firstapp.ui

import kotlinx.serialization.Serializable

// Navigation Routes
@Serializable object LoginRoute

@Serializable object RegisterRoute

@Serializable
object RoomListRoute // Màn hình danh sách phòng sau khi login

@Serializable object CreateRoomRoute

@Serializable
object JoinRoomRoute // Màn hình nhập mã phòng để join

@Serializable data class RoomDetailRoute(val roomId: String, val roomCode: String)

@Serializable data class AddExpenseRoute(val roomId: String)

@Serializable data class BalanceRoute(val roomId: String)

@Serializable object MainRoute

@Serializable object AddPersonalExpenseRoute
