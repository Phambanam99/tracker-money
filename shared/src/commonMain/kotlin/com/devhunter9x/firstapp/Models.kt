import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val roomId: String,         // ID phòng 🏠
    val payerId: String,        // ID người trả tiền 👤
    val amount: Double,         // Số tiền 💰
    val description: String,    // Lý do chi tiêu 📝
    val participantIds: List<String>, // Danh sách ID những người dùng chung 👥
    val timestamp: String       // Thời gian chi ⏰
)

@Serializable
data class User(
    val id: String,
    val name: String
)

@Serializable
data class Room(
    val id: String,
    val code: String,   // Mã phòng để join (VD: "P101")
    val name: String    // Tên phòng
)

@Serializable
data class Balance(
    val fromUser: User,     // Người nợ
    val toUser: User,       // Người được nhận
    val amount: Double      // Số tiền nợ
)

// ===== Request DTOs =====

@Serializable
data class CreateRoomRequest(
    val name: String,
    val code: String
)

@Serializable
data class JoinRoomRequest(
    val name: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val name: String,
    val roomCode: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val roomCode: String,
    val password: String
)

@Serializable
data class CreateExpenseRequest(
    val payerId: String,
    val amount: Double,
    val description: String,
    val participantIds: List<String>
)

// ===== Response DTOs =====

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
    val roomId: String,
    val roomCode: String
)

@Serializable
data class ErrorResponse(
    val message: String
)