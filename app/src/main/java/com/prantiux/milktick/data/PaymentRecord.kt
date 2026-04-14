package com.prantiux.milktick.data

import java.time.LocalDateTime
import java.time.YearMonth

enum class PaymentRecordType {
    PAYMENT,
    ADJUSTMENT,
    REFUND
}

data class PaymentRecord(
    val id: String,
    val userId: String,
    val amount: Double,
    val note: String = "",
    val recordedAt: LocalDateTime,
    val appliedYearMonth: YearMonth,
    val type: PaymentRecordType = PaymentRecordType.PAYMENT
)
