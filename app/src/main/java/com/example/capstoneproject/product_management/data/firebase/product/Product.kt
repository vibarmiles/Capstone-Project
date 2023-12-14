package com.example.capstoneproject.product_management.data.firebase.product

import com.google.firebase.database.ServerValue

data class Product(
    var image: String? = null,
    val productName: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val category: String? = null,
    val lastEdit: Any = ServerValue.TIMESTAMP,
    val supplier: String = "",
    val leadTime: Int = 3,
    val stock: Map<String, Int> = mapOf(),
    val transaction: Transaction = Transaction(),
    val active: Boolean = true
)

data class Transaction(
    val openingStock: Int = 0,
    val closingStock: Int = 0,
    val purchased: Int = 0,
    val soldThisYear: Int = 0,
    val soldThisMonth: Int = 0,
    val monthlySales: Map<String, Map<String, Int>> = mapOf()
)