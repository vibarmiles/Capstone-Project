package com.example.capstoneproject.point_of_sales.data.firebase

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class SalesInvoiceRepository : ISalesInvoiceRepository {
    private val firestore = Firebase.firestore
    private val salesInvoiceCollectionReference = firestore.collection("sales_invoices")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Invoice>> {
        val si = MutableLiveData<List<Invoice>>()
        salesInvoiceCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                si.value = value.toObjects()
                callback.invoke()
            }
        }
        return si
    }

    override fun insert(invoice: Invoice, result: (FirebaseResult) -> Unit) {
        if (invoice.id.isNotBlank()) {
            var check = true
            firestore.runTransaction {
                val snapshot = it.get(salesInvoiceCollectionReference.document(invoice.id)).toObject<Invoice>()
                if (snapshot != null) {
                    for (value in snapshot.products) {
                        if (!(!value.value.returned && invoice.products.let { products -> if (products.containsKey(value.key)) products[value.key]!!.returned else false })) {
                            check = false
                            break
                        }
                    }

                    if (check) {
                        it.set(salesInvoiceCollectionReference.document(invoice.id), invoice, SetOptions.merge())
                    } else {
                        result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                        return@runTransaction
                    }
                }
                it.set(salesInvoiceCollectionReference.document(invoice.id), invoice, SetOptions.merge())
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
                var check = true
                firestore.runTransaction {
                    val snapshot = it.get(salesInvoiceCollectionReference.document(invoice.originalInvoiceId)).toObject<Invoice>()
                    if (snapshot != null) {
                        for (value in snapshot.products) {
                            if (!(!value.value.returned && invoice.products.let { products -> if (products.containsKey(value.key)) products[value.key]!!.returned else false })) {
                                check = false
                                break
                            }
                        }

                        if (check) {
                            it.set(salesInvoiceCollectionReference.document(invoice.id), invoice, SetOptions.merge())
                        } else {
                            result.invoke(FirebaseResult(result = false, errorMessage = "Document waiting to be unlocked..."))
                            return@runTransaction
                        }
                    }
                    it.set(salesInvoiceCollectionReference.document(invoice.id), invoice, SetOptions.merge())
                }.addOnSuccessListener {
                    if (check) {
                        result.invoke(FirebaseResult(result = true))
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