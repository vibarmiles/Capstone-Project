package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class PurchaseOrderRepository : IPurchaseOrderRepository {
    private val firestore = Firebase.firestore
    private val purchaseOrderCollectionReference = firestore.collection("purchase_orders")

    override fun getAll(): MutableLiveData<List<PurchaseOrder>> {
        val po = MutableLiveData<List<PurchaseOrder>>()
        purchaseOrderCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                po.value = value.toObjects()
            }
        }
        return po
    }

    override fun insert(purchaseOrder: PurchaseOrder) {
        if (purchaseOrder.id.isNotBlank()) {
            purchaseOrderCollectionReference.document(purchaseOrder.id).set(purchaseOrder, SetOptions.merge())
        } else {
            purchaseOrderCollectionReference.add(purchaseOrder)
        }
    }

    override fun delete(purchaseOrder: PurchaseOrder) {
        purchaseOrderCollectionReference.document(purchaseOrder.id).delete()
    }
}