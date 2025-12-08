package com.prantiux.milktick.data

import java.time.YearMonth

data class MonthlySummary(
    val yearMonth: YearMonth,
    val totalDays: Int,
    val totalLiters: Float,
    val totalCost: Float,
    val userId: String
) {
    constructor() : this(
        yearMonth = YearMonth.now(),
        totalDays = 0,
        totalLiters = 0f,
        totalCost = 0f,
        userId = ""
    )
} 