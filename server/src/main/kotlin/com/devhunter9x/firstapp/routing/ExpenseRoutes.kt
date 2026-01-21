package com.devhunter9x.firstapp.routing

import com.devhunter9x.firstapp.CreateExpenseRequest
import com.devhunter9x.firstapp.config.getUserId
import com.devhunter9x.firstapp.domain.service.ExpenseService
import com.devhunter9x.firstapp.infrastructure.repository.ExpenseRepositoryImpl
import com.devhunter9x.firstapp.infrastructure.repository.RoomMemberRepositoryImpl
import com.devhunter9x.firstapp.infrastructure.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.abs

fun Route.expenseRoutes() {
    val expenseRepository = ExpenseRepositoryImpl()
    val userRepository = UserRepositoryImpl()
    val roomMemberRepository = RoomMemberRepositoryImpl()
    val expenseService = ExpenseService(expenseRepository, userRepository)

    // Protected routes - yêu cầu JWT token
    authenticate("auth-jwt") {
        route("/rooms/{roomId}/expenses") {
            // Lấy danh sách chi tiêu của phòng
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getUserId()
                    val roomId =
                            call.parameters["roomId"]
                                    ?: return@get call.respond(
                                            HttpStatusCode.BadRequest,
                                            mapOf("error" to "Thiếu roomId")
                                    )

                    // Kiểm tra user có thuộc phòng này không (qua RoomMembersTable)
                    if (!roomMemberRepository.isMember(userId, roomId)) {
                        return@get call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Bạn không phải thành viên của phòng này")
                        )
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
                    val userId = principal.getUserId()
                    val roomId =
                            call.parameters["roomId"]
                                    ?: return@post call.respond(
                                            HttpStatusCode.BadRequest,
                                            mapOf("error" to "Thiếu roomId")
                                    )

                    // Kiểm tra user có thuộc phòng này không
                    if (!roomMemberRepository.isMember(userId, roomId)) {
                        return@post call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Bạn không phải thành viên của phòng này")
                        )
                    }

                    val request = call.receive<CreateExpenseRequest>()

                    // Validate: nếu không chia đều thì tổng amounts phải bằng tổng expense
                    if (!request.splitEqually) {
                        val totalParticipantAmounts = request.participantAmounts.values.sum()
                        if (abs(totalParticipantAmounts - request.amount) > 0.01) {
                            return@post call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf(
                                            "error" to
                                                    "Tổng số tiền của các thành viên (${totalParticipantAmounts}) phải bằng tổng chi tiêu (${request.amount})"
                                    )
                            )
                        }
                    }

                    val expense =
                            expenseService.createExpense(
                                    roomId = roomId,
                                    payerId = request.payerId,
                                    amount = request.amount,
                                    description = request.description,
                                    participantAmounts = request.participantAmounts,
                                    splitEqually = request.splitEqually
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
                val userId = principal.getUserId()
                val roomId =
                        call.parameters["roomId"]
                                ?: return@get call.respond(
                                        HttpStatusCode.BadRequest,
                                        mapOf("error" to "Thiếu roomId")
                                )

                // Kiểm tra user có thuộc phòng này không
                if (!roomMemberRepository.isMember(userId, roomId)) {
                    return@get call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Bạn không phải thành viên của phòng này")
                    )
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
                val userId = principal.getUserId()
                val roomId =
                        call.parameters["roomId"]
                                ?: return@get call.respond(
                                        HttpStatusCode.BadRequest,
                                        mapOf("error" to "Thiếu roomId")
                                )

                println("DEBUG: Getting members for room=$roomId, user=$userId")

                // Kiểm tra user có thuộc phòng này không - nếu chưa thì tự động thêm
                if (!roomMemberRepository.isMember(userId, roomId)) {
                    println("DEBUG: User $userId not in room $roomId, auto-adding...")
                    roomMemberRepository.addMemberToRoom(userId, roomId)
                }

                // Lấy members qua RoomMembersTable
                val members = roomMemberRepository.getMembersByRoomId(roomId)
                println("DEBUG: Found ${members.size} members: ${members.map { it.name }}")
                call.respond(members)
            } catch (e: Exception) {
                println("DEBUG ERROR: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
