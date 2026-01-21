package com.devhunter9x.firstapp.domain.service

import Room
import com.devhunter9x.firstapp.domain.repository.RoomRepository
import com.devhunter9x.firstapp.exception.RoomAlreadyExistsException
import com.devhunter9x.firstapp.exception.RoomNotFoundException
import java.util.UUID

class RoomService(private val roomRepository: RoomRepository) {

    suspend fun createRoom(name: String, code: String): Room {
        // Kiểm tra mã phòng đã tồn tại chưa
        val existingRoom = roomRepository.findByCode(code)
        if (existingRoom != null) {
            throw RoomAlreadyExistsException("Phòng với mã '$code' đã tồn tại")
        }

        val newRoom = Room(
            id = UUID.randomUUID().toString(),
            code = code,
            name = name
        )

        roomRepository.create(newRoom)
        return newRoom
    }

    suspend fun getRoomByCode(code: String): Room {
        return roomRepository.findByCode(code)
            ?: throw RoomNotFoundException("Không tìm thấy phòng với mã '$code'")
    }

    suspend fun getRoomById(id: String): Room {
        return roomRepository.findById(id)
            ?: throw RoomNotFoundException("Không tìm thấy phòng với id '$id'")
    }

    suspend fun getAllRooms(): List<Room> {
        return roomRepository.getAll()
    }
}
