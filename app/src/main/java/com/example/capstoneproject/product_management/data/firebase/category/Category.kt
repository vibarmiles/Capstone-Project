package com.example.capstoneproject.product_management.data.firebase.category

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId val id: String = "",
    val categoryName: String = "",
    val active: Boolean = true
)