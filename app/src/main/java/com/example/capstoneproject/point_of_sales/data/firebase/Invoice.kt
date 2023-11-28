package com.example.capstoneproject.point_of_sales.data.firebase

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Invoice(
    @DocumentId val id: String = "",
    val originalInvoiceId: String = "",
    val invoiceType: InvoiceType = InvoiceType.SALE,
    @ServerTimestamp val date: Date? = null,
    val branchId: String = "",
    val userId: String = "",
    val lock: Boolean = false,
    val payment: Payment = Payment.CASH,
    val products: Map<String, Product> = mapOf()
)

data class Product(
    val id: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val supplier: String = "",
    val returned: Boolean = false
)

enum class InvoiceType {
    SALE, EXCHANGE, REFUND
}

enum class Payment {
    CASH, BANK, GCASH
}