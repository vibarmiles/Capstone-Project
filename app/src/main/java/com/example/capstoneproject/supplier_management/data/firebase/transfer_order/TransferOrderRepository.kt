package com.example.capstoneproject.supplier_management.data.firebase.transfer_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class TransferOrderRepository : ITransferOrderRepository {
    private val firestore = Firebase.firestore
    private val transferOrderCollectionReference = firestore.collection("transfer_orders")
    private var query = transferOrderCollectionReference.orderBy("date", Query.Direction.DESCENDING)
    private lateinit var document: DocumentSnapshot
    private var to = MutableLiveData<List<TransferOrder>>()

    override fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit
    ): MutableLiveData<List<TransferOrder>> {
        val current = to.value?.toMutableList()

        if (this::document.isInitialized) {
            query = query.startAfter(document)
        }

        transferOrderCollectionReference.limit(10).addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                if (it.size() > 0) {
                    document = it.documents[it.size() - 1]
                }

                if (current.isNullOrEmpty()) {
                    to.value = it.toObjects()
                } else {
                    for (queryDocumentSnapshot in it) {
                        val new = queryDocumentSnapshot.toObject<TransferOrder>()
                        current.firstOrNull { transferOrder -> transferOrder.id == new.id }.let { found ->
                            if (found != null) {
                                current[current.indexOf(found)] = new
                            } else {
                                current.add(new)
                            }
                        }
                    }

                    to.value = current
                }

                callback.invoke(it.size())
            }
        }
        return to
    }

    override fun insert(transferOrder: TransferOrder, fail: Boolean, result: (FirebaseResult) -> Unit) {
        if (transferOrder.id.isNotBlank()) {
            var check = false
            firestore.runTransaction {
                val snapshot = it.get(transferOrderCollectionReference.document(transferOrder.id)).toObject<TransferOrder>()
                if (snapshot != null) {
                    if (snapshot.status == Status.WAITING || fail) {
                        check = true
                        it.set(transferOrderCollectionReference.document(transferOrder.id), transferOrder, SetOptions.merge())
                    } else {
                        result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                        return@runTransaction
                    }
                }
            }.addOnSuccessListener {
                if (check) {
                    result.invoke(FirebaseResult(result = true))
                }
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