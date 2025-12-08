package com.prantiux.milktick.data

import java.time.YearMonth

data class MonthlyRate(
    val yearMonth: YearMonth,
    val ratePerLiter: Float,
    val defaultQuantity: Float,
    val userId: String
) {
    constructor() : this(
        yearMonth = YearMonth.now(),
        ratePerLiter = 0f,
        defaultQuantity = 0f,
        userId = ""
    )
} 