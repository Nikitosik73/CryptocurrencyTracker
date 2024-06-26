package ru.paramonov.cryptocurrencytracker.presentation.screens.deteilcoin.terminal

enum class TimeFrame(val value: String) {
    MIN_5(value = "5/minute"),
    MIN_15(value = "15/minute"),
    MIN_30(value = "30/minute"),
    HOUR_1(value = "1/hour"),
    DAY_1(value = "1/day")
}