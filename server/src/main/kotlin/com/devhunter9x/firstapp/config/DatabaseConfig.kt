package com.devhunter9x.firstapp.config

import com.devhunter9x.firstapp.infrastructure.persistence.ExpenseParticipantsTable
import com.devhunter9x.firstapp.infrastructure.persistence.ExpensesTable
import com.devhunter9x.firstapp.infrastructure.persistence.RoomsTable
import com.devhunter9x.firstapp.infrastructure.persistence.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    fun init() {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            // Đọc từ environment variables, fallback về giá trị mặc định
            jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/money_tracker"
            username = System.getenv("DB_USER") ?: "postgres"
            password = System.getenv("DB_PASSWORD") ?: "expense_tracker_2024"

            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            
            // Thêm timeout để đợi DB khởi động
            connectionTimeout = 30000 // 30 giây
            initializationFailTimeout = 60000 // 60 giây
            
            validate()
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        // Tự động tạo tất cả các bảng
        transaction {
            SchemaUtils.create(
                RoomsTable,
                UsersTable,
                ExpensesTable,
                ExpenseParticipantsTable
            )
        }
    }
}