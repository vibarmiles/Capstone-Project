package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import androidx.lifecycle.MutableLiveData

interface IPurchaseOrderRepository {
    fun getAll(callback: () -> Unit): MutableLiveData<List<PurchaseOrder>>
    fun insert(purchaseOrder: PurchaseOrder)
    fun delete(purchaseOrder: PurchaseOrder)
}