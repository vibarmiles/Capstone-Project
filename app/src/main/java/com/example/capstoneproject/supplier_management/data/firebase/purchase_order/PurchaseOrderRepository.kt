package com.example.capstoneproject.supplier_management.data.firebase.purchase_order

import android.util.Log
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

class PurchaseOrderRepository : IPurchaseOrderRepository {
    private val firestore = Firebase.firestore
    private val purchaseOrderCollectionReference = firestore.collection("purchase_orders")
    private lateinit var document: DocumentSnapshot
    private var query = purchaseOrderCollectionReference.orderBy("date", Query.Direction.DESCENDING)
    private  val po = MutableLiveData<List<PurchaseOrder>>()
    private var currentSize = 10

    override fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<PurchaseOrder>> {
        if (this::document.isInitialized) {
            query = query.startAfter(document)
        }

        query.limit(10).addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                val current = po.value?.toMutableList() ?: mutableListOf()

                if (it.size() > 0) {
                    document = it.documents[it.size() - 1]
                    currentSize = it.size()
                } else {
                    currentSize = 0
                }

                for (queryDocumentSnapshot in it) {
                    val new = queryDocumentSnapshot.toObject<PurchaseOrder>()
                    current.firstOrNull { purchaseOrder -> purchaseOrder.id == new.id }.let { found ->
                        if (found != null) {
                            current[current.indexOf(found)] = new
                        } else {
                            current.add(new)
                        }
                    }
                }

                po.value = current

                Log.e("Current Size", document.toString())
                if (currentSize > 0) {
                    callback.invoke(it.size())
                } else {
                    callback.invoke(0)
                }
            }
        }
        return po
    }

    override fun getWaiting(result: (FirebaseResult) -> Unit): MutableLiveData<List<PurchaseOrder>> {
        val waitingPO: MutableLiveData<List<PurchaseOrder>> = MutableLiveData<List<PurchaseOrder>>()
        purchaseOrderCollectionReference.whereEqualTo("status", "WAITING").addSnapshotListener { value, error ->
            error?. let {
                return@addSnapshotListener
            }

            value?.let {
                waitingPO.value = value.toObjects()
                result.invoke(FirebaseResult(result = true))
            }
        }
        return waitingPO
    }

    override fun insert(purchaseOrder: PurchaseOrder, fail: Boolean, result: (FirebaseResult) -> Unit) {
        if (purchaseOrder.id.isNotBlank()) {
            var check = false
            firestore.runTransaction {
                val snapshot = it.get(purchaseOrderCollectionReference.document(purchaseOrder.id)).toObject<PurchaseOrder>()
                if (snapshot != null) {
                    if (snapshot.status == Status.WAITING || fail) {
                        check = true
                        Log.e("SETTING", "SETTING")
                        it.set(purchaseOrderCollectionReference.document(purchaseOrder.id), purchaseOrder, SetOptions.merge())
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
            purchaseOrderCollectionReference.count().get(AggregateSource.SERVER).addOnSuccessListener {
                if ((it.count + 1) > 100) {
                    purchaseOrderCollectionReference.orderBy("date").limit((it.count + 1) - 100).get().addOnSuccessListener { snapshot ->
                        firestore.runBatch { batch ->
                            val documents = po.value?.toMutableList()
                            for (document in snapshot.toObjects<PurchaseOrder>()) {
                                batch.delete(purchaseOrderCollectionReference.document(document.id))
                                documents?.remove(document)
                            }
                            po.value = documents
                        }
                    }
                }
            }

            purchaseOrderCollectionReference.add(purchaseOrder).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }
}