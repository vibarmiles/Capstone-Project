package com.example.capstoneproject.product_management.ui.product

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.product_management.data.firebase.product.IProductRepository
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private lateinit var products: SnapshotStateMap<String, Product>
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)

    fun getAll(): SnapshotStateMap<String, Product> {
        if (!this::products.isInitialized) {
            products = productRepository.getAll { updateLoadingState() }
        }

        return products
    }

    fun insert(id: String? = null, product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.insert(key = id, product = product)
        }
    }

    fun delete(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.delete(key = key)
        }
    }

    fun setStock(key: String, value: Map<String, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.setQuantityForBranch(key = key, value = value)
        }
    }

    fun removeCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.removeCategory(categoryId = categoryId)
        }
    }

    fun removeBranchStock(branchId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.removeBranchStock(branchId = branchId)
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }
}