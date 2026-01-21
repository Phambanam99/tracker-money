package com.devhunter9x.firstapp.routing

import CreateRoomRequest
import com.devhunter9x.firstapp.domain.service.RoomService
import com.devhunter9x.firstapp.exception.RoomAlreadyExistsException
import com.devhunter9x.firstapp.exception.RoomNotFoundException
import com.devhunter9x.firstapp.infrastructure.repository.RoomRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.roomRoutes() {
    val roomRepository = RoomRepositoryImpl()
    val roomService = RoomService(roomRepository)

    route("/rooms") {
        // Tạo phòng mới
        post {
            try {
                val request = call.receive<CreateRoomRequest>()
                val room = roomService.createRoom(request.name, request.code)
                call.respond(HttpStatusCode.Created, room)
            } catch (e: RoomAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Lấy danh sách tất cả phòng
        get {
            val rooms = roomService.getAllRooms()
            call.respond(rooms)
        }

        // Lấy thông tin phòng theo mã
        get("/{code}") {
            try {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Thiếu mã phòng")
                val room = roomService.getRoomByCode(code)
                call.respond(room)
            } catch (e: RoomNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }
    }
}
