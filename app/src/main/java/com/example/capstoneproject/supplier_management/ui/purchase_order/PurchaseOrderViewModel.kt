package com.example.capstoneproject.supplier_management.ui.purchase_order

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
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.IPurchaseOrderRepository
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PurchaseOrderViewModel : ViewModel() {
    private lateinit var purchaseOrders: MutableLiveData<List<PurchaseOrder>>
    private val purchaseOrderRepository: IPurchaseOrderRepository = PurchaseOrderRepository()
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()
    val returnSize = mutableStateOf(0)

    fun getAll(): MutableLiveData<List<PurchaseOrder>> {
        if (!this::purchaseOrders.isInitialized) {
            purchaseOrders = purchaseOrderRepository.getAll(callback = { updateLoadingState(); returnSize.value = it }) {
                    result ->
                resultState.update { result }
            }
        }

        return purchaseOrders
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseOrders = purchaseOrderRepository.getAll(callback = { returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }
    }

    fun insert(purchaseOrder: PurchaseOrder, fail: Boolean = false, returnResult: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseOrderRepository.insert(purchaseOrder = purchaseOrder, fail = fail) { result ->
                if (returnResult) {
                    resultState.update { result }
                }
            }
        }
    }

    fun delete(purchaseOrder: PurchaseOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseOrderRepository.delete(purchaseOrder = purchaseOrder) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getDocument(id: String): PurchaseOrder? {
        return purchaseOrders.value?.firstOrNull { purchaseOrder -> purchaseOrder.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }

    fun transact(document: PurchaseOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            insert(purchaseOrder = document, returnResult = false)
            productRepository.transact(document = document) { result ->
                viewModelScope.launch {
                    if (!result.result) {
                        insert(purchaseOrder = document.copy(status = Status.FAILED), returnResult = false, fail = true)
                        Log.e("TRANSACTION", "FAILED")
                    } else {
                        insert(purchaseOrder = document.copy(status = Status.COMPLETE), returnResult = false, fail = true)
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