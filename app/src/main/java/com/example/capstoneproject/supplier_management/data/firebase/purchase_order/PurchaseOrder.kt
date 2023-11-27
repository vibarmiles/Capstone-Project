package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class PurchaseOrder(
    @DocumentId val id: String = "",
    @ServerTimestamp val date: Date? = null,
    val branchId: String = "",
    val status: Status = Status.WAITING,
    val products: Map<String, Product> = mapOf()
)