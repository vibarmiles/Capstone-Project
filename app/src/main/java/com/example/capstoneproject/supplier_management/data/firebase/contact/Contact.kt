package com.example.capstoneproject.supplier_management.data.firebase.contact

data class Contact(
    val name: String = "",
    val contact: String = "",
    val product: Map<String, Double> = mutableMapOf()
)
