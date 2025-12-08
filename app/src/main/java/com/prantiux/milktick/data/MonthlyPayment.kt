package com.prantiux.milktick.data

import java.time.YearMonth

data class MonthlyPayment(
    val yearMonth: YearMonth,
    val userId: String,
    val isPaid: Boolean = false,
    val paymentNote: String = "",
    val paidDate: Long? = null // Timestamp when marked as paid
)
