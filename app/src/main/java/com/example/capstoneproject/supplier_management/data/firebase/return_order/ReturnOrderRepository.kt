package com.example.capstoneproject.supplier_management.data.firebase.return_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.google.firebase.firestore.AggregateSource
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
    private val ro = MutableLiveData<List<ReturnOrder>>()
    private var currentSize = 10

    override fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<ReturnOrder>> {
        if (this::document.isInitialized) {
            query = query.startAfter(document)
        }

        query.limit(10).addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                val current = ro.value?.toMutableList() ?: mutableListOf()

                if (it.size() > 0) {
                    document = it.documents[it.size() - 1]
                    currentSize = it.size()
                } else {
                    currentSize = 0
                }

                for (queryDocumentSnapshot in it) {
                    try {
                        val new = queryDocumentSnapshot.toObject<ReturnOrder>()
                        current.firstOrNull { returnOrder -> returnOrder.id == new.id }.let { found ->
                            if (found != null) {
                                current[current.indexOf(found)] = new
                            } else {
                                current.add(new)
                            }
                        }
                    } catch (e: Exception) {
                        result.invoke(FirebaseResult(errorMessage = e.message))
                    }
                }

                ro.value = current

                if (currentSize > 0) {
                    callback.invoke(it.size())
                } else {
                    callback.invoke(0)
                }
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

                        if (snapshot.products != returnOrder.products) {
                            it.delete(returnOrderCollectionReference.document(returnOrder.id))
                        }

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
            returnOrderCollectionReference.count().get(AggregateSource.SERVER).addOnSuccessListener {
                if ((it.count + 1) > 100) {
                    returnOrderCollectionReference.orderBy("date").limit((it.count + 1) - 100).get().addOnSuccessListener { snapshot ->
                        firestore.runBatch { batch ->
                            val documents = ro.value?.toMutableList()
                            for (document in snapshot.toObjects<ReturnOrder>()) {
                                batch.delete(returnOrderCollectionReference.document(document.id))
                                documents?.remove(document)
                            }
                            ro.value = documents
                        }
                    }
                }
            }

            returnOrderCollectionReference.add(returnOrder).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }
}