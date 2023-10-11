package com.example.capstoneproject.product_management.ui.product

import androidx.compose.runtime.snapshots.SnapshotStateMap
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

    fun getAll(): SnapshotStateMap<String, Product> {
        if (!this::products.isInitialized) {
            products = productRepository.getAll()
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
}