package com.example.capstoneproject.supplier_management.data.firebase.transfer_order

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface ITransferOrderRepository {
    fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<TransferOrder>>
    fun insert(transferOrder: TransferOrder, fail: Boolean, result: (FirebaseResult) -> Unit)
    fun delete(transferOrder: TransferOrder, result: (FirebaseResult) -> Unit)
}