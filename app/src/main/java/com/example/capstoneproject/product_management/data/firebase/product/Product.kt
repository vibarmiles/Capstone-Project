package com.example.capstoneproject.product_management.data.firebase.product

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId val id: String = "",
    val image: String?,
    val productName: String,
    val price: Double,
    val category: String = "",
    val quantity: Int
)
