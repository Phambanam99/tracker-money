package com.devhunter9x.firstapp.routing

import LoginRequest
import RegisterRequest
import AuthResponse
import com.devhunter9x.firstapp.domain.service.AuthService
import com.devhunter9x.firstapp.exception.InvalidCredentialsException
import com.devhunter9x.firstapp.exception.UserAlreadyExistsException
import com.devhunter9x.firstapp.infrastructure.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val userRepository = UserRepositoryImpl()
    val authService = AuthService(userRepository)

    route("/auth") {
        // Đăng ký user mới (không cần room)
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result = authService.register(request.name, request.password)
                
                call.respond(HttpStatusCode.Created, AuthResponse(
                    token = result.token,
                    user = result.user
                ))
            } catch (e: UserAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // Đăng nhập (không cần room)
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = authService.login(request.name, request.password)
                
                call.respond(AuthResponse(
                    token = result.token,
                    user = result.user
                ))
            } catch (e: InvalidCredentialsException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
