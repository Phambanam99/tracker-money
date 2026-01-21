package com.devhunter9x.firstapp.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "expense-tracker-secret-key-2024"
    private val issuer = System.getenv("JWT_ISSUER") ?: "expense-tracker"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "expense-tracker-users"
    private val algorithm = Algorithm.HMAC256(secret)
    
    // Token hết hạn sau 7 ngày
    private const val VALIDITY_IN_MS = 7 * 24 * 60 * 60 * 1000L

    fun generateToken(userId: String, userName: String, roomId: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("userName", userName)
            .withClaim("roomId", roomId)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
            .sign(algorithm)
    }

    fun Application.configureJwt() {
        install(Authentication) {
            jwt("auth-jwt") {
                realm = "expense-tracker"
                verifier(
                    JWT.require(algorithm)
                        .withIssuer(issuer)
                        .withAudience(audience)
                        .build()
                )
                validate { credential ->
                    val userId = credential.payload.getClaim("userId").asString()
                    val userName = credential.payload.getClaim("userName").asString()
                    val roomId = credential.payload.getClaim("roomId").asString()
                    
                    if (userId != null && userName != null && roomId != null) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }
    }
}

// Extension để lấy thông tin user từ JWT
fun JWTPrincipal.getUserId(): String = payload.getClaim("userId").asString()
fun JWTPrincipal.getUserName(): String = payload.getClaim("userName").asString()
fun JWTPrincipal.getRoomId(): String = payload.getClaim("roomId").asString()
