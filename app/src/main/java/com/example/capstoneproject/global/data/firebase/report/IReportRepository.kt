package com.example.capstoneproject.global.data.firebase.report

import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice

interface IReportRepository {
    fun setMonthReport(id: String, report: Report)
    fun setValues(document: Invoice, result: (FirebaseResult) -> Unit)
}