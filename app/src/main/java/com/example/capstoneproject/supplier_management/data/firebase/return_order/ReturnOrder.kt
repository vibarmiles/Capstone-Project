package com.example.capstoneproject.supplier_management.data.firebase.return_order

import com.example.capstoneproject.supplier_management.data.firebase.Product
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.google.firebase.firestore.DocumentId

data class ReturnOrder(
    @DocumentId val id: String = "",
    val date: String = "",
    val status: Status = Status.WAITING,
    val reason: String = "",
    val branchId: String = "",
    val products: Map<String, Product> = mapOf()
)