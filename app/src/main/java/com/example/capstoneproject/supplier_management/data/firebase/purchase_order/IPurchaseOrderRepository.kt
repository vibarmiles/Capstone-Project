package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface IPurchaseOrderRepository {
    fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<PurchaseOrder>>
    fun insert(purchaseOrder: PurchaseOrder, fail: Boolean, result: (FirebaseResult) -> Unit)
    fun delete(purchaseOrder: PurchaseOrder, result: (FirebaseResult) -> Unit)
}