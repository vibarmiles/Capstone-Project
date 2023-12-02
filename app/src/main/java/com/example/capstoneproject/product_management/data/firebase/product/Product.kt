package com.example.capstoneproject.product_management.data.firebase.product

import com.google.firebase.database.ServerValue
import java.time.Month

data class Product(
    var image: String? = null,
    val productName: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val category: String? = null,
    val lastEdit: Any = ServerValue.TIMESTAMP,
    val supplier: String = "",
    val leadTime: Int = 0,
    val stock: Map<String, Int> = mapOf(),
    val transaction: Transaction = Transaction(),
    val isActive: Boolean = true
)

data class Transaction(
    val purchased: Int = 0,
    val soldThisYear: Int = 0,
    val soldThisMonth: Int = 0,
    val monthlySales: Map<String, Int> = mapOf()
)