package com.example.capstoneproject.supplier_management.ui.transfer_order

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.product_management.data.firebase.product.IProductRepository
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.ITransferOrderRepository
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrder
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferOrderViewModel : ViewModel() {
    lateinit var transferOrders: MutableLiveData<List<TransferOrder>>
    private val transferOrderRepository: ITransferOrderRepository = TransferOrderRepository()
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()
    val returnSize = mutableStateOf(0)

    fun getAll(): MutableLiveData<List<TransferOrder>> {
        if (!this::transferOrders.isInitialized) {
            transferOrders = transferOrderRepository.getAll(callback = { updateLoadingState(); returnSize.value = it }) {
                    result ->
                resultState.update { result }
            }
        }

        return transferOrders
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            transferOrders = transferOrderRepository.getAll(callback = { returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }
    }

    fun insert(transferOrder: TransferOrder, returnResult: Boolean = true, fail: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            transferOrderRepository.insert(transferOrder = transferOrder, fail = fail) {
                    result ->
                if (returnResult) {
                    resultState.update { result }
                }
            }
        }
    }

    fun delete(transferOrder: TransferOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            transferOrderRepository.delete(transferOrder = transferOrder) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getDocument(id: String): TransferOrder? {
        return transferOrders.value?.firstOrNull { transferOrder -> transferOrder.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }

    fun transact(document: TransferOrder) {
        Log.e("TRANSACTION", "STARTED")
        viewModelScope.launch(Dispatchers.IO) {
            if (document.status == Status.CANCELLED) {
                insert(transferOrder = document)
            } else {
                insert(transferOrder = document.copy(status = Status.PENDING), returnResult = false)
                productRepository.transact(document = document) { result ->
                    viewModelScope.launch {
                        if (!result.result) {
                            insert(transferOrder = document.copy(status = Status.FAILED), returnResult = false, fail = true)
                            Log.e("TRANSACTION", "FAILED")
                        } else {
                            insert(transferOrder = document.copy(status = Status.COMPLETE), returnResult = false, fail = true)
                            Log.e("TRANSACTION", "FINISHED")
                        }
                        delay(1000)
                    }.let {
                        if (it.isCompleted) {
                            resultState.update { result }
                        }
                    }
                }
            }
        }
    }
}