package com.example.capstoneproject.point_of_sales.data.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class SalesInvoiceRepository : ISalesInvoiceRepository {
    private val firestore = Firebase.firestore
    private val salesInvoiceCollectionReference = firestore.collection("sales_invoices")
    private var query = salesInvoiceCollectionReference.orderBy("date", Query.Direction.DESCENDING)
    private lateinit var document: DocumentSnapshot
    private val si = MutableLiveData<List<Invoice>>()
    private var currentSize = 10


    override fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Invoice>> {
        if (this::document.isInitialized) {
            query = query.startAfter(document)
        }

        query.limit(10).addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                val current = si.value?.toMutableList() ?: mutableListOf()

                if (it.size() > 0) {
                    document = it.documents[it.size() - 1]
                    currentSize = if (currentSize == 10) it.size() else 0
                }

                for (queryDocumentSnapshot in it) {
                    val new = queryDocumentSnapshot.toObject<Invoice>()
                    current.firstOrNull { invoice -> invoice.id == new.id }.let { found ->
                        if (found != null) {
                            current[current.indexOf(found)] = new
                        } else {
                            current.add(new)
                        }
                    }
                }

                si.value = current

                if (currentSize > 0) {
                    callback.invoke(it.size())
                } else {
                    callback.invoke(0)
                }
            }
        }

        return si
    }

    override fun insert(invoice: Invoice, access: Boolean, result: (FirebaseResult) -> Unit) {
        if (invoice.id.isNotBlank()) {
            var check = false
            firestore.runTransaction {
                val snapshot = it.get(salesInvoiceCollectionReference.document(invoice.id)).toObject<Invoice>()
                if (snapshot != null) {
                    Log.d("SNAPSHOT", snapshot.toString())
                    if (!snapshot.lock || access) {
                        check = true
                        Log.d("SNAPSHOT", snapshot.toString())
                        it.set(salesInvoiceCollectionReference.document(invoice.id), invoice.copy(lock = false), SetOptions.merge())
                    } else {
                        result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                        return@runTransaction
                    }
                } else {
                    result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                    return@runTransaction
                }
            }.addOnSuccessListener {
                if (check) {
                    result.invoke(FirebaseResult(result = true))
                }
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            if (invoice.invoiceType == InvoiceType.SALE) {
                salesInvoiceCollectionReference.add(invoice).addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            } else {
                var check = false
                firestore.runTransaction {
                    val snapshot = it.get(salesInvoiceCollectionReference.document(invoice.originalInvoiceId)).toObject<Invoice>()
                    if (snapshot != null) {
                        Log.d("SNAPSHOT", snapshot.toString())
                        if (!snapshot.lock || access) {
                            check = true
                            Log.d("SNAPSHOT", snapshot.toString())
                            it.set(salesInvoiceCollectionReference.document(invoice.originalInvoiceId), snapshot.copy(lock = snapshot.lock.not()), SetOptions.merge())
                        } else {
                            result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                            return@runTransaction
                        }
                    } else {
                        result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                        return@runTransaction
                    }
                }.addOnSuccessListener {
                    if (check && !access) {
                        Log.d("ADDING NEW DOCUMENT", invoice.toString().uppercase())
                        salesInvoiceCollectionReference.add(invoice).addOnSuccessListener {
                            Log.d("ADDING NEW DOCUMENT", "SUCCESS")
                            result.invoke(FirebaseResult(result = true))
                        }.addOnFailureListener {
                            Log.d("ADDING NEW DOCUMENT", "FAILED")
                            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                        }
                    } else {
                        result.invoke(FirebaseResult(errorMessage = "Error Occurred!"))
                    }
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        }
    }

    override fun delete(invoice: Invoice, result: (FirebaseResult) -> Unit) {
        salesInvoiceCollectionReference.document(invoice.id).delete().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}