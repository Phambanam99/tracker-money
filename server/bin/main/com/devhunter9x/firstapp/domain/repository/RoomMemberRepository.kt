package com.devhunter9x.firstapp.domain.repository

import User

interface RoomMemberRepository {
    suspend fun addMemberToRoom(userId: String, roomId: String): Boolean
    suspend fun removeMemberFromRoom(userId: String, roomId: String): Boolean
    suspend fun getMembersByRoomId(roomId: String): List<User>
    suspend fun getRoomIdsByUserId(userId: String): List<String>
    suspend fun isMember(userId: String, roomId: String): Boolean
}
