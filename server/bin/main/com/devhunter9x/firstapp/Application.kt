package com.devhunter9x.firstapp

import com.devhunter9x.firstapp.config.DatabaseConfig
import com.devhunter9x.firstapp.config.JwtConfig.configureJwt
import com.devhunter9x.firstapp.routing.authRoutes
import com.devhunter9x.firstapp.routing.expenseRoutes
import com.devhunter9x.firstapp.routing.roomRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Cấu hình Content Negotiation (JSON)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // 2. Cấu hình CORS cho Web/Mobile clients
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // Trong production nên giới hạn origins
    }

    // 3. Cấu hình JWT Authentication
    configureJwt()

    // 4. Khởi tạo Database
    DatabaseConfig.init()

    // 5. Định tuyến (Routing)
    routing {
        // Health check
        get("/") {
            call.respond(mapOf("status" to "ok", "message" to "Expense Tracker API"))
        }

        get("/health") {
            call.respond(mapOf("status" to "healthy"))
        }

        // API Routes
        authRoutes()      // /auth/register, /auth/login
        roomRoutes()      // /rooms
        expenseRoutes()   // /rooms/{roomId}/expenses, /rooms/{roomId}/balances
    }
}