package com.example.capstoneproject.product_management.ui.product

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.product_management.data.firebase.product.IProductRepository
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private lateinit var products: SnapshotStateMap<String, Product>
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    val update = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): SnapshotStateMap<String, Product> {
        if (!this::products.isInitialized) {
            products = productRepository.getAll(callback = { updateLoadingState() }, update = {
                update.value = update.value.not()
                Log.e("Update", "Finished ${update.value}")
            }) {
                    result ->
                resultState.update { result }
            }
        }

        return products
    }

    fun insert(id: String? = null, product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.insert(key = id, product = product) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(key: String, product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.delete(key = key, product = product) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun setStock(key: String, value: Map<String, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.setQuantityForBranch(key = key, value = value) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun removeCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.removeCategory(categoryId = categoryId) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun removeBranchStock(branchId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.removeBranchStock(branchId = branchId) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getProduct(id: String?): Product? {
        return products[id]
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }
}