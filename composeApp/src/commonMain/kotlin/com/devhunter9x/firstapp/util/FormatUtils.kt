package com.devhunter9x.firstapp.util

/** Utility functions for Kotlin Multiplatform compatibility */

// Format số với 0 chữ số thập phân (thay thế String.format("%.0f", value))
fun Double.formatNoDecimals(): String {
    return this.toLong().toString()
}

// Format timestamp thành ngày tháng đơn giản
fun Long.formatTimestamp(): String {
    if (this <= 0) return ""

    // Tính toán ngày tháng từ epoch milliseconds
    val totalSeconds = this / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    // Tính từ epoch (1/1/1970)
    var remainingDays = totalDays.toInt()
    var year = 1970

    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (remainingDays < daysInYear) break
        remainingDays -= daysInYear
        year++
    }

    val daysInMonths =
            if (isLeapYear(year)) intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            else intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    var month = 0
    while (month < 12 && remainingDays >= daysInMonths[month]) {
        remainingDays -= daysInMonths[month]
        month++
    }
    month++ // 1-indexed
    val day = remainingDays + 1

    val hours = ((totalHours % 24) + 7) % 24 // UTC+7
    val minutes = totalMinutes % 60

    return "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${year} " +
            "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

// Format ngày từ DatePicker millis
fun Long.formatDate(): String {
    if (this <= 0) return "Hôm nay"

    val totalDays = (this / 1000 / 60 / 60 / 24).toInt()
    var remainingDays = totalDays
    var year = 1970

    while (true) {
        val daysInYear = if (isLeapYear(year)) 366 else 365
        if (remainingDays < daysInYear) break
        remainingDays -= daysInYear
        year++
    }

    val daysInMonths =
            if (isLeapYear(year)) intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            else intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    var month = 0
    while (month < 12 && remainingDays >= daysInMonths[month]) {
        remainingDays -= daysInMonths[month]
        month++
    }
    month++
    val day = remainingDays + 1

    return "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${year}"
}

// Format tiền tệ có dấu phẩy ngăn cách (VD: 1,000,000)
fun Double.formatCurrency(): String {
    val longVal = this.toLong()
    return longVal.toString().reversed().chunked(3).joinToString(",").reversed()
}

// Format chuỗi số input (VD: "1000000" -> "1,000,000")
fun String.formatInputMoney(): String {
    if (this.isBlank()) return ""
    val digits = this.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return digits.reversed().chunked(3).joinToString(",").reversed()
}

// Parse string formatted money back to Double
fun String.parseMoney(): Double? {
    return this.replace(",", "").toDoubleOrNull()
}
