package com.example.capstoneproject.global.data.firebase.report

import android.util.Log
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.user_management.data.firebase.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.*
import java.util.Date
import kotlin.math.log

class ReportRepository : IReportRepository {
    private val firebase = Firebase.database.reference
    private val reportReference = firebase.child("report")

    override fun setMonthReport(id: String, report: Report) {
        Log.e("TRANSACTION", "READY")
        reportReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                Log.e("TRANSACTION", "START")

                currentData.getValue<Report>().let {
                    if (it == null) {
                        currentData.value = Report()
                        return Transaction.success(currentData)
                    }

                    firebase.child("users").child(id).get().addOnSuccessListener {  value ->
                        value.getValue<User>().let { user ->
                            val date = Instant.ofEpochMilli(it.timestamp as Long).atZone(ZoneId.systemDefault()).toLocalDate()
                            val loginDate = Instant.ofEpochMilli(user!!.lastLogin as Long).atZone(ZoneId.systemDefault()).toLocalDate()

                            if (date < loginDate) {
                                currentData.value = report.copy(
                                    timestamp = it.timestamp + (LocalDate.of(loginDate.year, loginDate.month, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond() - it.timestamp),
                                    lastBeginningMonthStock = it.beginningMonthStock,
                                    highestSoldMonth = if (it.highestSoldMonth < it.soldCurrentMonth) it.soldCurrentMonth else it.highestSoldMonth,
                                    lowestSoldMonth = if (it.lowestSoldMonth > it.soldCurrentMonth) it.soldCurrentMonth else it.lowestSoldMonth,
                                )
                            }
                        }
                    }
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {

            }
        })
    }

    override fun setValues(document: Invoice, result: (FirebaseResult) -> Unit) {
        reportReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = currentData.getValue<Report>()!!.let {
                    when (document.invoiceType) {
                        InvoiceType.SALE -> {
                            val totalSold = it.soldCurrentMonth + document.products.values.sumOf { product -> product.quantity }
                            it.copy(soldCurrentMonth = totalSold)
                        }

                        InvoiceType.EXCHANGE -> {
                            Transaction.abort()
                        }

                        InvoiceType.REFUND -> {
                            val totalSold = it.soldCurrentMonth - document.products.values.sumOf { product -> product.quantity }
                            it.copy(soldCurrentMonth = totalSold)
                        }
                    }
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {

            }
        })
    }
}