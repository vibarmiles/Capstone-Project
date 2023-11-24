package com.example.capstoneproject.product_management.data.firebase.product

data class Product(
    var image: String? = null,
    val productName: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val category: String? = null,
    val supplier: String = "",
    val criticalLevel: Int = 0,
    val stock: Map<String, Int> = mapOf(),
    val transaction: Transaction = Transaction(),
    val isActive: Boolean = true
)

data class Transaction(
    val purchased: Int = 0,
    val returned: Int = 0,
    val sold: Int = 0
)

enum class Type {
    PURCHASE, SELL, RETURN
}