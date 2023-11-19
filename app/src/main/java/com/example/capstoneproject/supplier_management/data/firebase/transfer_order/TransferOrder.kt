package com.example.capstoneproject.supplier_management.data.firebase.transfer_order

import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.Product
import com.google.firebase.firestore.DocumentId

data class TransferOrder(
    @DocumentId val id: String = "",
    val date: String = "",
    val status: Status = Status.WAITING,
    val oldBranchId: String = "",
    val destinationBranchId: String = "",
    val products: Map<String, Product> = mapOf()
)
