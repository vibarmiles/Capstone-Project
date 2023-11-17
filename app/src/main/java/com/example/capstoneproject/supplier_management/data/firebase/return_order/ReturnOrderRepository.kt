package com.example.capstoneproject.supplier_management.data.firebase.return_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class ReturnOrderRepository : IReturnOrderRepository {
    private val firestore = Firebase.firestore
    private val returnOrderCollectionReference = firestore.collection("return_orders")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<ReturnOrder>> {
        val ro = MutableLiveData<List<ReturnOrder>>()
        returnOrderCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                ro.value = value.toObjects()
                callback.invoke()
            }
        }
        return ro
    }

    override fun insert(returnOrder: ReturnOrder, result: (FirebaseResult) -> Unit) {
        if (returnOrder.id.isNotBlank()) {
            returnOrderCollectionReference.document(returnOrder.id).set(returnOrder, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            returnOrderCollectionReference.add(returnOrder).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(returnOrder: ReturnOrder, result: (FirebaseResult) -> Unit) {
        returnOrderCollectionReference.document(returnOrder.id).delete().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}