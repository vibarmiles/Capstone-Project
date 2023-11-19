package com.example.capstoneproject.supplier_management.ui.transfer_order

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.ITransferOrderRepository
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrder
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferOrderViewModel : ViewModel() {
    lateinit var transferOrders: MutableLiveData<List<TransferOrder>>
    private val transferOrderRepository: ITransferOrderRepository = TransferOrderRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<TransferOrder>> {
        if (!this::transferOrders.isInitialized) {
            transferOrders = transferOrderRepository.getAll(callback = { updateLoadingState() }) {
                    result ->
                resultState.update { result }
            }
        }

        return transferOrders
    }

    fun insert(transferOrder: TransferOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            transferOrderRepository.insert(transferOrder = transferOrder) {
                    result ->
                resultState.update { result }
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
}