package com.example.capstoneproject.point_of_sales.ui

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.point_of_sales.data.firebase.ISalesInvoiceRepository
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.point_of_sales.data.firebase.SalesInvoiceRepository
import com.example.capstoneproject.product_management.data.firebase.product.IProductRepository
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class POSViewModel : ViewModel() {
    lateinit var salesInvoices: MutableLiveData<List<Invoice>>
    private val salesInvoiceRepository: ISalesInvoiceRepository = SalesInvoiceRepository()
    private val productRepository: IProductRepository = ProductRepository()
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

    fun insert(invoice: Invoice, returnResult: Boolean = true, access: Boolean = false, callback: (FirebaseResult) -> Unit = {  }) {
        viewModelScope.launch(Dispatchers.IO) {
            salesInvoiceRepository.insert(invoice = invoice, access = access) { result ->
                if (returnResult) {
                    resultState.update { result }
                }

                callback.invoke(result)
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

    fun transact(document: Invoice) {
        if (document.invoiceType != InvoiceType.SALE) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            productRepository.transact(document = document) { result ->
                viewModelScope.launch {
                    if (!result.result) {
                        Log.e("TRANSACTION", "FAILED")
                    } else {
                        insert(invoice = document)
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

    fun returnAndExchange(document: Invoice) {
        if (document.invoiceType == InvoiceType.SALE) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.e("TRANSACTION", "BEGIN ${document.toString().uppercase()}")
            insert(invoice = document, returnResult = false) { current ->
                Log.e("TRANSACTION", "ORIGINAL INVOICE ${current.result.toString().uppercase()}")
                if (current.result) {
                    productRepository.transact(document = document) { result ->
                        Log.e("TRANSACTION", "PRODUCTS ${result.result.toString().uppercase()}")
                        viewModelScope.launch {
                            if (result.result) {
                                insert(getDocument(document.originalInvoiceId)!!.let { it.copy(products = it.products.toMutableMap().let { products ->
                                    Log.e("TRANSACTION", "ORIGINAL DOCUMENT ${it.toString().uppercase()}")
                                    for (currentProduct in products) {
                                        if (document.products.any { product -> product.value.id == currentProduct.value.id}) {
                                            products[currentProduct.key] = currentProduct.value.copy(returned = true)
                                        }
                                    }

                                    products
                                }) }, access = true)
                                Log.e("TRANSACTION", "FINISHED")
                            } else {
                                insert(invoice = document, access = true)
                                Log.e("TRANSACTION", "FAILED")
                            }
                        }.let { job ->
                            if (job.isCompleted) {
                                resultState.update { result }
                            }
                        }
                    }
                } else {
                    resultState.update { current }
                }
            }
        }
    }
}