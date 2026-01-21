package com.devhunter9x.firstapp.api

import AuthResponse
import Balance
import CreateExpenseRequest
import Expense
import LoginRequest
import RegisterRequest
import Room
import User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(private val baseUrl: String = "http://10.0.2.2:8081") {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    
    private var authToken: String? = null
    
    fun setToken(token: String) {
        authToken = token
    }
    
    fun clearToken() {
        authToken = null
    }
    
    // ===== Auth APIs =====
    
    suspend fun register(name: String, roomCode: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, roomCode, password))
            }
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                authToken = authResponse.token
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Register failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(name: String, roomCode: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(name, roomCode, password))
            }
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                authToken = authResponse.token
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ===== Room APIs =====
    
    suspend fun createRoom(name: String, code: String): Result<Room> {
        return try {
            val response = client.post("$baseUrl/rooms") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("name" to name, "code" to code))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Create room failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoomByCode(code: String): Result<Room> {
        return try {
            val response = client.get("$baseUrl/rooms/$code")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Room not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ===== Expense APIs (Protected) =====
    
    suspend fun getExpenses(roomId: String): Result<List<Expense>> {
        return try {
            val response = client.get("$baseUrl/rooms/$roomId/expenses") {
                authToken?.let { bearerAuth(it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Get expenses failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createExpense(
        roomId: String,
        payerId: String,
        amount: Double,
        description: String,
        participantIds: List<String>
    ): Result<Expense> {
        return try {
            val response = client.post("$baseUrl/rooms/$roomId/expenses") {
                contentType(ContentType.Application.Json)
                authToken?.let { bearerAuth(it) }
                setBody(CreateExpenseRequest(payerId, amount, description, participantIds))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Create expense failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBalances(roomId: String): Result<List<Balance>> {
        return try {
            val response = client.get("$baseUrl/rooms/$roomId/balances") {
                authToken?.let { bearerAuth(it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Get balances failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMembers(roomId: String): Result<List<User>> {
        return try {
            val response = client.get("$baseUrl/rooms/$roomId/members") {
                authToken?.let { bearerAuth(it) }
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Get members failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
