package com.prantiux.milktick.data

import java.time.LocalDate

data class MilkEntry(
    val date: LocalDate,
    val quantity: Float,
    val brought: Boolean,
    val note: String?,
    val userId: String
) {
    constructor() : this(
        date = LocalDate.now(),
        quantity = 0f,
        brought = false,
        note = null,
        userId = ""
    )
} 