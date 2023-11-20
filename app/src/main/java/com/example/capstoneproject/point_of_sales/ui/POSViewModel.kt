package com.example.capstoneproject.point_of_sales.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.point_of_sales.data.firebase.ISalesInvoiceRepository
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.SalesInvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class POSViewModel : ViewModel() {
    lateinit var salesInvoices: MutableLiveData<List<Invoice>>
    private val salesInvoiceRepository: ISalesInvoiceRepository = SalesInvoiceRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<Invoice>> {
        if (!this::salesInvoices.isInitialized) {
            salesInvoices = salesInvoiceRepository.getAll(callback = { updateLoadingState() }) {
                    result ->
                resultState.update { result }
            }
        }

        return salesInvoices
    }

    fun insert(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            salesInvoiceRepository.insert(invoice = invoice) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            salesInvoiceRepository.delete(invoice = invoice) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getDocument(id: String): Invoice? {
        return salesInvoices.value?.firstOrNull { invoice -> invoice.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }
}