package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class PurchaseOrderRepository : IPurchaseOrderRepository {
    private val firestore = Firebase.firestore
    private val purchaseOrderCollectionReference = firestore.collection("purchase_orders")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<PurchaseOrder>> {
        val po = MutableLiveData<List<PurchaseOrder>>()
        purchaseOrderCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                po.value = value.toObjects()
                callback.invoke()
            }
        }
        return po
    }

    override fun insert(purchaseOrder: PurchaseOrder, result: (FirebaseResult) -> Unit) {
        if (purchaseOrder.id.isNotBlank()) {
            purchaseOrderCollectionReference.document(purchaseOrder.id).set(purchaseOrder, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            purchaseOrderCollectionReference.add(purchaseOrder).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(purchaseOrder: PurchaseOrder, result: (FirebaseResult) -> Unit) {
        purchaseOrderCollectionReference.document(purchaseOrder.id).delete().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}