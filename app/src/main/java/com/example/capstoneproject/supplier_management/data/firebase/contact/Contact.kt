package com.example.capstoneproject.supplier_management.data.firebase.contact

import com.google.firebase.firestore.DocumentId

data class Contact(
    @DocumentId val id: String = "",
    val name: String = "",
    val contact: String = "",
    val active: Boolean = true
)
