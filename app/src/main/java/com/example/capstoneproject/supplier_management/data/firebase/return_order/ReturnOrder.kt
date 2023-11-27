package com.example.capstoneproject.supplier_management.data.firebase.return_order

import com.example.capstoneproject.supplier_management.data.firebase.Product
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ReturnOrder(
    @DocumentId val id: String = "",
    @ServerTimestamp val date: Date? = null,
    val status: Status = Status.WAITING,
    val reason: String = "",
    val branchId: String = "",
    val products: Map<String, Product> = mapOf()
)