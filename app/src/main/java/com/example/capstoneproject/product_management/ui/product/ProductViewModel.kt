package com.example.capstoneproject.product_management.ui.product

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    var products = mutableStateListOf<Product>()
    private val productRepository: ProductRepository = ProductRepository()

    init {
        products = productRepository.getAll()
    }

    fun insert(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.insert(product = product)
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.delete(product = product)
        }
    }
}