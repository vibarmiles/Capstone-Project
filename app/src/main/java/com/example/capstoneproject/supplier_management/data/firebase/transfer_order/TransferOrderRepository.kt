package com.example.capstoneproject.supplier_management.data.firebase.transfer_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class TransferOrderRepository : ITransferOrderRepository {
    private val firestore = Firebase.firestore
    private val transferOrderCollectionReference = firestore.collection("transfer_orders")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit
    ): MutableLiveData<List<TransferOrder>> {
        val to = MutableLiveData<List<TransferOrder>>()
        transferOrderCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                to.value = value.toObjects()
                callback.invoke()
            }
        }
        return to
    }

    override fun insert(transferOrder: TransferOrder, result: (FirebaseResult) -> Unit) {
        if (transferOrder.id.isNotBlank()) {
            transferOrderCollectionReference.document(transferOrder.id).set(transferOrder, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            transferOrderCollectionReference.add(transferOrder).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(transferOrder: TransferOrder, result: (FirebaseResult) -> Unit) {
        transferOrderCollectionReference.document(transferOrder.id).delete().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}