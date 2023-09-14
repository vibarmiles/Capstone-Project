package com.example.capstoneproject.product_management.data.firebase.product

import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.google.firebase.database.Exclude

data class Product(
    var image: String? = null,
    val productName: String = "",
    val price: Double = 0.0,
    val category: String? = null,
    val criticalLevel: Int = 0,
    val stock: Map<String, Int> = mapOf()
)
