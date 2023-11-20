package com.example.capstoneproject.point_of_sales.data.firebase

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface ISalesInvoiceRepository {
    fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Invoice>>
    fun insert(invoice: Invoice, result: (FirebaseResult) -> Unit)
    fun delete(invoice: Invoice, result: (FirebaseResult) -> Unit)
}