package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PurchaseOrderViewModel : ViewModel() {
    lateinit var purchaseOrders: MutableLiveData<List<PurchaseOrder>>
    private val purchaseOrderRepository: PurchaseOrderRepository = PurchaseOrderRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<PurchaseOrder>> {
        if (!this::purchaseOrders.isInitialized) {
            purchaseOrders = purchaseOrderRepository.getAll(callback = { updateLoadingState() }) {
                    result ->
                resultState.update { result }
            }
        }

        return purchaseOrders
    }

    fun insert(purchaseOrder: PurchaseOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseOrderRepository.insert(purchaseOrder = purchaseOrder) {
                    result ->
                resultState.update { result }
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
}