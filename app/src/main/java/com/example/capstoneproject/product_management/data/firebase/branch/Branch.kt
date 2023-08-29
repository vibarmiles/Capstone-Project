package com.example.capstoneproject.product_management.data.firebase.branch

import com.google.firebase.firestore.DocumentId

data class Branch(
    @DocumentId val id: String = "",
    val name: String = "",
    val address: String = ""
)
