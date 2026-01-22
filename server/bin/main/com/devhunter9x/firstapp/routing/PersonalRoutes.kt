package com.devhunter9x.firstapp.routing

import com.devhunter9x.firstapp.CreatePersonalExpenseRequest
import com.devhunter9x.firstapp.PersonalExpense
import com.devhunter9x.firstapp.config.getUserId
import com.devhunter9x.firstapp.infrastructure.repository.PersonalExpenseRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.personalRoutes() {
    val personalExpenseRepository = PersonalExpenseRepositoryImpl()

    authenticate("auth-jwt") {
        route("/personal/expenses") {
            // Get all personal expenses for the current user
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getUserId()

                    val expenses = personalExpenseRepository.findByUserId(userId)
                    call.respond(expenses)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // Create a new personal expense
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.getUserId()
                    val request = call.receive<CreatePersonalExpenseRequest>()

                    val newExpense =
                            PersonalExpense(
                                    id = UUID.randomUUID().toString(),
                                    amount = request.amount,
                                    category = request.category,
                                    note = request.note,
                                    date = request.date,
                                    type = request.type
                            )

                    val created = personalExpenseRepository.create(userId, newExpense)
                    if (created) {
                        call.respond(HttpStatusCode.Created, newExpense)
                    } else {
                        call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Failed to create expense")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
        }

        route("/personal/expenses/{id}") {
            delete {
                try {
                    // TODO: enhanced security check via delete(userId, expenseId)
                    val id =
                            call.parameters["id"]
                                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val deleted = personalExpenseRepository.delete(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
    }
}
