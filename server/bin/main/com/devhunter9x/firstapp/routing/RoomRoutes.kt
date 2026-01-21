package com.devhunter9x.firstapp.routing

import CreateRoomRequest
import JoinRoomRequest
import JoinRoomResponse
import RoomListResponse
import com.devhunter9x.firstapp.config.JwtConfig
import com.devhunter9x.firstapp.config.getUserId
import com.devhunter9x.firstapp.config.getUserName
import com.devhunter9x.firstapp.domain.service.RoomService
import com.devhunter9x.firstapp.exception.RoomAlreadyExistsException
import com.devhunter9x.firstapp.exception.RoomNotFoundException
import com.devhunter9x.firstapp.infrastructure.repository.RoomMemberRepositoryImpl
import com.devhunter9x.firstapp.infrastructure.repository.RoomRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.roomRoutes() {
    val roomRepository = RoomRepositoryImpl()
    val roomMemberRepository = RoomMemberRepositoryImpl()
    val roomService = RoomService(roomRepository)

    route("/rooms") {
        // Tạo phòng mới (public - không cần auth)
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

        // Lấy thông tin phòng theo mã (public)
        get("/{code}") {
            try {
                val code =
                        call.parameters["code"] ?: throw IllegalArgumentException("Thiếu mã phòng")
                val room = roomService.getRoomByCode(code)
                call.respond(room)
            } catch (e: RoomNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        // ===== Protected Routes (cần auth) =====
        authenticate("auth-jwt") {
            // Tham gia phòng bằng mã
            post("/join") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getUserId()
                    val userName = principal.getUserName()

                    val request = call.receive<JoinRoomRequest>()
                    val room =
                            roomRepository.findByCode(request.roomCode)
                                    ?: throw RoomNotFoundException(
                                            "Không tìm thấy phòng với mã '${request.roomCode}'"
                                    )

                    // Kiểm tra xem user đã là member chưa
                    if (roomMemberRepository.isMember(userId, room.id)) {
                        // Đã là member, trả về token với roomId
                        val token = JwtConfig.generateTokenWithRoom(userId, userName, room.id)
                        call.respond(JoinRoomResponse(room = room, token = token))
                        return@post
                    }

                    // Thêm user vào phòng
                    roomMemberRepository.addMemberToRoom(userId, room.id)

                    // Tạo token mới với roomId context
                    val token = JwtConfig.generateTokenWithRoom(userId, userName, room.id)

                    call.respond(
                            HttpStatusCode.Created,
                            JoinRoomResponse(room = room, token = token)
                    )
                } catch (e: RoomNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            // Lấy danh sách phòng của user hiện tại
            get("/my") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getUserId()

                    val roomIds = roomMemberRepository.getRoomIdsByUserId(userId)
                    val rooms = roomIds.mapNotNull { roomRepository.findById(it) }

                    call.respond(RoomListResponse(rooms = rooms))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }
    }
}
