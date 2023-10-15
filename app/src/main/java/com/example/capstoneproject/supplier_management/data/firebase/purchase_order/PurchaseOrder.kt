package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import com.google.firebase.firestore.DocumentId

data class PurchaseOrder(
    @DocumentId val id: String = "",
    val supplier: String = "",
    val date: String = "",
    val status: Status = Status.WAITING,
    val products: Map<String, Product> = mapOf()
)