package com.example.capstoneproject.supplier_management.ui.return_order

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
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
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<ReturnOrder>> {
        if (!this::returnOrders.isInitialized) {
            returnOrders = returnOrderRepository.getAll(callback = { updateLoadingState() }) {
                    result ->
                resultState.update { result }
            }
        }

        return returnOrders
    }

    fun insert(returnOrder: ReturnOrder) {
        viewModelScope.launch(Dispatchers.IO) {
            returnOrderRepository.insert(returnOrder = returnOrder) {
                    result ->
                resultState.update { result }
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
}