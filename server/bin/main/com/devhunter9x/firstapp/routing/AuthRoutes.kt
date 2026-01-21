package com.devhunter9x.firstapp.routing

import LoginRequest
import RegisterRequest
import AuthResponse
import com.devhunter9x.firstapp.domain.service.AuthService
import com.devhunter9x.firstapp.exception.InvalidCredentialsException
import com.devhunter9x.firstapp.exception.RoomNotFoundException
import com.devhunter9x.firstapp.exception.UserAlreadyExistsException
import com.devhunter9x.firstapp.infrastructure.repository.RoomRepositoryImpl
import com.devhunter9x.firstapp.infrastructure.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val userRepository = UserRepositoryImpl()
    val roomRepository = RoomRepositoryImpl()
    val authService = AuthService(userRepository, roomRepository)

    route("/auth") {
        // Đăng ký và join vào phòng
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result = authService.register(request.name, request.roomCode, request.password)
                
                call.respond(HttpStatusCode.Created, AuthResponse(
                    token = result.token,
                    user = result.user,
                    roomId = result.roomId,
                    roomCode = result.roomCode
                ))
            } catch (e: RoomNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            } catch (e: UserAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Đăng nhập
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = authService.login(request.name, request.roomCode, request.password)
                
                call.respond(AuthResponse(
                    token = result.token,
                    user = result.user,
                    roomId = result.roomId,
                    roomCode = result.roomCode
                ))
            } catch (e: RoomNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            } catch (e: InvalidCredentialsException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
