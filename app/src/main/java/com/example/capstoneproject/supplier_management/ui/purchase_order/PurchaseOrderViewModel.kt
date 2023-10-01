package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PurchaseOrderViewModel : ViewModel() {
    val purchaseOrders: MutableLiveData<List<PurchaseOrder>>
    private val purchaseOrderRepository: PurchaseOrderRepository = PurchaseOrderRepository()

    init {
        purchaseOrders = purchaseOrderRepository.getAll()
    }

    fun insert(purchaseOrder: PurchaseOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseOrderRepository.insert(purchaseOrder = purchaseOrder)
        }
    }

    fun delete(purchaseOrder: PurchaseOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseOrderRepository.delete(purchaseOrder = purchaseOrder)
        }
    }
}