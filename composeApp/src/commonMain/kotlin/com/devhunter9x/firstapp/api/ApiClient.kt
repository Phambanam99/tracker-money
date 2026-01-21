package com.devhunter9x.firstapp.api

import com.devhunter9x.firstapp.*
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
            json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
            )
        }
    }

    private var authToken: String? = null

    fun setToken(token: String) {
        authToken = token
    }

    fun clearToken() {
        authToken = null
    }

    fun hasToken(): Boolean = authToken != null

    // ===== Auth APIs =====

    suspend fun register(name: String, password: String): Result<AuthResponse> {
        return try {
            val response =
                    client.post("$baseUrl/auth/register") {
                        contentType(ContentType.Application.Json)
                        setBody(RegisterRequest(name, password))
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

    suspend fun login(name: String, password: String): Result<AuthResponse> {
        return try {
            val response =
                    client.post("$baseUrl/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest(name, password))
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
            val response =
                    client.post("$baseUrl/rooms") {
                        contentType(ContentType.Application.Json)
                        setBody(CreateRoomRequest(name, code))
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

    suspend fun joinRoom(roomCode: String): Result<JoinRoomResponse> {
        return try {
            val response =
                    client.post("$baseUrl/rooms/join") {
                        contentType(ContentType.Application.Json)
                        authToken?.let { bearerAuth(it) }
                        setBody(JoinRoomRequest(roomCode))
                    }
            if (response.status.isSuccess()) {
                val joinResponse = response.body<JoinRoomResponse>()
                // Cập nhật token mới có roomId context
                authToken = joinResponse.token
                Result.success(joinResponse)
            } else {
                Result.failure(Exception("Join room failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyRooms(): Result<List<Room>> {
        return try {
            val response = client.get("$baseUrl/rooms/my") { authToken?.let { bearerAuth(it) } }
            if (response.status.isSuccess()) {
                val roomList = response.body<RoomListResponse>()
                Result.success(roomList.rooms)
            } else {
                Result.failure(Exception("Get rooms failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== Expense APIs (Protected) =====

    suspend fun getExpenses(roomId: String): Result<List<Expense>> {
        return try {
            val response =
                    client.get("$baseUrl/rooms/$roomId/expenses") {
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
            participantAmounts: Map<String, Double>,
            splitEqually: Boolean = true
    ): Result<Expense> {
        return try {
            val response =
                    client.post("$baseUrl/rooms/$roomId/expenses") {
                        contentType(ContentType.Application.Json)
                        authToken?.let { bearerAuth(it) }
                        setBody(
                                CreateExpenseRequest(
                                        payerId,
                                        amount,
                                        description,
                                        participantAmounts,
                                        splitEqually
                                )
                        )
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
            val response =
                    client.get("$baseUrl/rooms/$roomId/balances") {
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
            val response =
                    client.get("$baseUrl/rooms/$roomId/members") {
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

    // ===== Personal Finance APIs =====

    suspend fun getPersonalExpenses(): Result<List<PersonalExpense>> {
        return try {
            val response =
                    client.get("$baseUrl/personal/expenses") { authToken?.let { bearerAuth(it) } }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Get personal expenses failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPersonalExpense(
            request: CreatePersonalExpenseRequest
    ): Result<PersonalExpense> {
        return try {
            val response =
                    client.post("$baseUrl/personal/expenses") {
                        contentType(ContentType.Application.Json)
                        authToken?.let { bearerAuth(it) }
                        setBody(request)
                    }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Create personal expense failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePersonalExpense(id: String): Result<Boolean> {
        return try {
            val response =
                    client.delete("$baseUrl/personal/expenses/$id") {
                        authToken?.let { bearerAuth(it) }
                    }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Delete personal expense failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
