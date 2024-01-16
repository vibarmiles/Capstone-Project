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
import com.example.capstoneproject.supplier_management.data.firebase.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class POSViewModel : ViewModel() {
    private lateinit var salesInvoices: MutableLiveData<List<Invoice>>
    private val salesInvoiceRepository: ISalesInvoiceRepository = SalesInvoiceRepository()
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()
    val returnSize = mutableStateOf(0)
    var taken = mutableStateOf(false)

    fun getAll(): MutableLiveData<List<Invoice>> {
        if (!this::salesInvoices.isInitialized) {
            salesInvoices = salesInvoiceRepository.getAll(callback = { updateLoadingState(); returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }

        return salesInvoices
    }

    fun getCurrent(): MutableLiveData<List<Invoice>> {
        if (!taken.value) {
            salesInvoices = salesInvoiceRepository.getAll(callback = { updateLoadingState(); returnSize.value = it }) { result ->
                resultState.update { result }
            }
            taken.value = true
        }

        return salesInvoices
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            salesInvoices = salesInvoiceRepository.getAll(callback = { returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }
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

    private fun updateLoadingState() {
        isLoading.value = false
        Log.e("LOADING", isLoading.value.toString())
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
                        insert(invoice = document.copy(status = Status.FAILED))
                        Log.e("TRANSACTION", "FAILED")
                    } else {
                        insert(invoice = document.copy(status = Status.COMPLETE))
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

    fun transactFromWaiting(document: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            if (document.status == Status.CANCELLED) {
                insert(invoice = document)
            } else {
                if (document.invoiceType == InvoiceType.SALE) {
                    transact(document = document.copy(status = Status.PENDING))
                } else {
                    returnAndExchange(document = document.copy(status = Status.PENDING))
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
            insert(invoice = document.copy(status = Status.COMPLETE), returnResult = false) { current ->
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
                                insert(invoice = document.copy(status = Status.FAILED), access = true)
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