package com.example.capstoneproject.supplier_management.data.firebase.return_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface IReturnOrderRepository {
    fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<ReturnOrder>>
    fun insert(returnOrder: ReturnOrder, fail: Boolean, result: (FirebaseResult) -> Unit)
}