package com.example.spendly.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatMinorUnits(minor: Long, currencyCode: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    runCatching { format.currency = Currency.getInstance(currencyCode) }
    return format.format(minor / 100.0)
}

fun parseAmountToMinor(input: String): Long? {
    val normalized = input.trim().replace(",", ".")
    if (normalized.isEmpty()) return null
    return runCatching {
        val value = normalized.toBigDecimal()
        if (value <= java.math.BigDecimal.ZERO) return null
        value.multiply(java.math.BigDecimal(100)).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()
}
