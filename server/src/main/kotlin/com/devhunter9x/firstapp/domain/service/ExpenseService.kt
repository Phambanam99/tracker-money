package com.devhunter9x.firstapp.domain.service

import Balance
import Expense
import User
import com.devhunter9x.firstapp.domain.repository.ExpenseRepository
import com.devhunter9x.firstapp.domain.repository.UserRepository
import java.util.UUID

class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val userRepository: UserRepository
) {

    suspend fun createExpense(
        roomId: String,
        payerId: String,
        amount: Double,
        description: String,
        participantIds: List<String>
    ): Expense {
        val expense = Expense(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            payerId = payerId,
            amount = amount,
            description = description,
            participantIds = participantIds,
            timestamp = System.currentTimeMillis().toString()
        )

        expenseRepository.create(expense)
        expenseRepository.addParticipants(expense.id, participantIds)

        return expense
    }

    suspend fun getExpensesByRoom(roomId: String): List<Expense> {
        return expenseRepository.findByRoomId(roomId)
    }

    /**
     * Tính toán công nợ giữa các thành viên trong phòng
     * Trả về danh sách Balance, mỗi Balance cho biết ai nợ ai bao nhiêu
     */
    suspend fun calculateBalances(roomId: String): List<Balance> {
        val expenses = expenseRepository.findByRoomId(roomId)
        val members = userRepository.findByRoomId(roomId)
        
        if (members.isEmpty()) return emptyList()

        // Map lưu số tiền mỗi người đã chi và được hưởng
        val paid = mutableMapOf<String, Double>()      // Số tiền đã trả
        val owed = mutableMapOf<String, Double>()      // Số tiền phải trả (được hưởng)

        members.forEach { 
            paid[it.id] = 0.0
            owed[it.id] = 0.0
        }

        // Tính toán từ các khoản chi
        for (expense in expenses) {
            val payerId = expense.payerId
            val participants = expense.participantIds
            val amountPerPerson = expense.amount / participants.size

            // Người trả đã chi thêm
            paid[payerId] = (paid[payerId] ?: 0.0) + expense.amount

            // Mỗi người tham gia nợ thêm
            for (participantId in participants) {
                owed[participantId] = (owed[participantId] ?: 0.0) + amountPerPerson
            }
        }

        // Tính số dư thực (positive = được nhận, negative = phải trả)
        val netBalance = mutableMapOf<String, Double>()
        members.forEach {
            netBalance[it.id] = (paid[it.id] ?: 0.0) - (owed[it.id] ?: 0.0)
        }

        // Tạo danh sách Balance từ người nợ -> người được nhận
        val balances = mutableListOf<Balance>()
        val debtors = netBalance.filter { it.value < 0 }.toMutableMap()
        val creditors = netBalance.filter { it.value > 0 }.toMutableMap()

        val memberMap = members.associateBy { it.id }

        for ((debtorId, debtAmount) in debtors.toList()) {
            var remainingDebt = -debtAmount

            for ((creditorId, creditAmount) in creditors.toList()) {
                if (remainingDebt <= 0) break
                if (creditAmount <= 0) continue

                val transferAmount = minOf(remainingDebt, creditAmount)
                
                if (transferAmount > 0.01) { // Bỏ qua số tiền quá nhỏ
                    balances.add(
                        Balance(
                            fromUser = memberMap[debtorId]!!,
                            toUser = memberMap[creditorId]!!,
                            amount = transferAmount
                        )
                    )
                }

                remainingDebt -= transferAmount
                creditors[creditorId] = creditAmount - transferAmount
            }
        }

        return balances
    }
}
