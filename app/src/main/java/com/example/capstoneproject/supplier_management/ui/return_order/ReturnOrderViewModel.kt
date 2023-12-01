package com.example.capstoneproject.supplier_management.ui.return_order

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
import com.example.capstoneproject.supplier_management.data.firebase.return_order.IReturnOrderRepository
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReturnOrderViewModel : ViewModel() {
    lateinit var returnOrders: MutableLiveData<List<ReturnOrder>>
    private val returnOrderRepository: IReturnOrderRepository = ReturnOrderRepository()
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()
    val returnSize = mutableStateOf(0)

    fun getAll(): MutableLiveData<List<ReturnOrder>> {
        if (!this::returnOrders.isInitialized) {
            returnOrders = returnOrderRepository.getAll(callback = { updateLoadingState(); returnSize.value = it }) {
                    result ->
                resultState.update { result }
            }
        }

        return returnOrders
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            returnOrders = returnOrderRepository.getAll(callback = { returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }
    }

    fun insert(returnOrder: ReturnOrder, returnResult: Boolean = true, fail: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            returnOrderRepository.insert(returnOrder = returnOrder, fail = fail) {
                    result ->
                if (returnResult) {
                    resultState.update { result }
                }
            }
        }
    }

    fun delete(returnOrder: ReturnOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            returnOrderRepository.delete(returnOrder = returnOrder) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getDocument(id: String): ReturnOrder? {
        return returnOrders.value?.firstOrNull { returnOrder -> returnOrder.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }

    fun transact(document: ReturnOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            if (document.status == Status.CANCELLED) {
                insert(returnOrder = document)
            } else {
                insert(returnOrder = document.copy(status = Status.PENDING), returnResult = false)
                productRepository.transact(document = document) { result ->
                    viewModelScope.launch {
                        if (!result.result) {
                            insert(returnOrder = document.copy(status = Status.FAILED), returnResult = false, fail = true)
                            Log.e("TRANSACTION", "FAILED")
                        } else {
                            insert(returnOrder = document.copy(status = Status.COMPLETE), returnResult = false, fail = true)
                            Log.e("TRANSACTION", "FINISHED")
                        }
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