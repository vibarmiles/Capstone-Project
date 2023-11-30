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
    val leadTime: Int = 0,
    val changeLeastSold: Boolean = true,
    val stock: Map<String, Int> = mapOf(),
    val transaction: Transaction = Transaction(),
    val isActive: Boolean = true
)

data class Transaction(
    val purchased: Int = 0,
    val soldThisYear: Int = 0,
    val soldLastYear: Int = 0,
    val soldThisMonth: Int = 0,
    val highestMonth: Int = 0,
    val lowestMonth: Int = 0,
)