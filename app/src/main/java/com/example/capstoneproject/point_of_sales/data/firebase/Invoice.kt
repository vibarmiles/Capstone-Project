package com.example.capstoneproject.point_of_sales.data.firebase

import com.google.firebase.firestore.DocumentId

data class Invoice(
    @DocumentId val id: String = "",
    val date: String = "",
    val branchId: String = "",
    val userId: String = "",
    val products: Map<String, Product> = mapOf()
)

data class Product(
    val id: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val supplier: String = ""
)