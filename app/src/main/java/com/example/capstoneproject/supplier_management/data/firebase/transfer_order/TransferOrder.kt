package com.example.capstoneproject.supplier_management.data.firebase.transfer_order

import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.Product
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class TransferOrder(
    @DocumentId val id: String = "",
    @ServerTimestamp val date: Date? = null,
    val status: Status = Status.WAITING,
    val oldBranchId: String = "",
    val destinationBranchId: String = "",
    val products: Map<String, Product> = mapOf()
)
