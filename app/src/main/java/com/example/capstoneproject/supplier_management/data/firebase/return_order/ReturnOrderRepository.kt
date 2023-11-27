package com.example.capstoneproject.supplier_management.data.firebase.return_order

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

class ReturnOrderRepository : IReturnOrderRepository {
    private val firestore = Firebase.firestore
    private val returnOrderCollectionReference = firestore.collection("return_orders")
    private var query = returnOrderCollectionReference.orderBy("date", Query.Direction.DESCENDING)
    private lateinit var document: DocumentSnapshot
    private  var ro = MutableLiveData<List<ReturnOrder>>()

    override fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<ReturnOrder>> {
        val current = ro.value?.toMutableList()

        if (this::document.isInitialized) {
            query = query.startAfter(document)
        }

        returnOrderCollectionReference.limit(10).addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                if (it.size() > 0) {
                    document = it.documents[it.size() - 1]
                }

                if (current.isNullOrEmpty()) {
                    ro.value = it.toObjects()
                } else {
                    for (queryDocumentSnapshot in it) {
                        val new = queryDocumentSnapshot.toObject<ReturnOrder>()
                        current.firstOrNull { returnOrder -> returnOrder.id == new.id }.let { found ->
                            if (found != null) {
                                current[current.indexOf(found)] = new
                            } else {
                                current.add(new)
                            }
                        }
                    }

                    ro.value = current
                }

                callback.invoke(it.size())
            }
        }
        return ro
    }

    override fun insert(returnOrder: ReturnOrder, fail: Boolean, result: (FirebaseResult) -> Unit) {
        if (returnOrder.id.isNotBlank()) {
            var check = false
            firestore.runTransaction {
                val snapshot = it.get(returnOrderCollectionReference.document(returnOrder.id)).toObject<ReturnOrder>()
                if (snapshot != null) {
                    if (snapshot.status == Status.WAITING || fail) {
                        check = true
                        it.set(returnOrderCollectionReference.document(returnOrder.id), returnOrder, SetOptions.merge())
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