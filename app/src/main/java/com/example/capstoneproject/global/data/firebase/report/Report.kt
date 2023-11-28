package com.example.capstoneproject.global.data.firebase.report

import com.google.firebase.Timestamp
import com.google.firebase.database.ServerValue

data class Report(
    val timestamp: Any = ServerValue.TIMESTAMP,
    val lastBeginningMonthStock: Int = 0,
    val beginningMonthStock: Int = 0,
    val highestSoldMonth: Int = 0,
    val lowestSoldMonth: Int = 0,
    val totalProductsSoldLastYear: Int = 0,
    val totalProductsSoldCurrentYear: Int = 0,
    val soldCurrentMonth: Int = 0
)
