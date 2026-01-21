package com.devhunter9x.firstapp.routing

import CreateExpenseRequest
import com.devhunter9x.firstapp.config.getRoomId
import com.devhunter9x.firstapp.domain.service.ExpenseService
import com.devhunter9x.firstapp.infrastructure.repository.ExpenseRepositoryImpl
import com.devhunter9x.firstapp.infrastructure.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.expenseRoutes() {
    val expenseRepository = ExpenseRepositoryImpl()
    val userRepository = UserRepositoryImpl()
    val expenseService = ExpenseService(expenseRepository, userRepository)

    // Protected routes - yêu cầu JWT token
    authenticate("auth-jwt") {
        route("/rooms/{roomId}/expenses") {
            // Lấy danh sách chi tiêu của phòng
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val roomId = call.parameters["roomId"] 
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Thiếu roomId"))
                    
                    // Kiểm tra user có thuộc phòng này không
                    val userRoomId = principal.getRoomId()
                    if (userRoomId != roomId) {
                        return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Bạn không có quyền truy cập phòng này"))
                    }

                    val expenses = expenseService.getExpensesByRoom(roomId)
                    call.respond(expenses)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // Tạo chi tiêu mới
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val roomId = call.parameters["roomId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Thiếu roomId"))
                    
                    val userRoomId = principal.getRoomId()
                    if (userRoomId != roomId) {
                        return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Bạn không có quyền truy cập phòng này"))
                    }

                    val request = call.receive<CreateExpenseRequest>()
                    val expense = expenseService.createExpense(
                        roomId = roomId,
                        payerId = request.payerId,
                        amount = request.amount,
                        description = request.description,
                        participantIds = request.participantIds
                    )
                    
                    call.respond(HttpStatusCode.Created, expense)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }

        // API tính công nợ
        get("/rooms/{roomId}/balances") {
            try {
                val principal = call.principal<JWTPrincipal>()!!
                val roomId = call.parameters["roomId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Thiếu roomId"))
                
                val userRoomId = principal.getRoomId()
                if (userRoomId != roomId) {
                    return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Bạn không có quyền truy cập phòng này"))
                }

                val balances = expenseService.calculateBalances(roomId)
                call.respond(balances)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // Lấy danh sách thành viên trong phòng
        get("/rooms/{roomId}/members") {
            try {
                val principal = call.principal<JWTPrincipal>()!!
                val roomId = call.parameters["roomId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Thiếu roomId"))
                
                val userRoomId = principal.getRoomId()
                if (userRoomId != roomId) {
                    return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Bạn không có quyền truy cập phòng này"))
                }

                val members = userRepository.findByRoomId(roomId)
                call.respond(members)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
