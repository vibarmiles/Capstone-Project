package com.example.capstoneproject.point_of_sales.data.firebase

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
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
            salesInvoiceCollectionReference.document(invoice.id).set(invoice, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            salesInvoiceCollectionReference.add(invoice).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
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