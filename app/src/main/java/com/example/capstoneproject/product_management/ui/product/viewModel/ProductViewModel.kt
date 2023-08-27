package com.example.capstoneproject.product_management.ui.product.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.Database
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.data.Room.branch.BranchRepository
import com.example.capstoneproject.product_management.data.Room.category.Category
import com.example.capstoneproject.product_management.data.Room.category.CategoryRepository
import com.example.capstoneproject.product_management.data.Room.product.Product
import com.example.capstoneproject.product_management.data.Room.product.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    val branches: Flow<List<Branch>>
    val categories: Flow<List<Category>>
    val products: Flow<Map<Category, List<Product>>>
    private val branchRepository: BranchRepository
    private val categoryRepository: CategoryRepository
    private val productRepository: ProductRepository

    init {
        val branchDao = Database.getDatabase(application).getBranchDao()
        branchRepository = BranchRepository(branchDao)
        branches = branchRepository.getAll()

        val categoryDao = Database.getDatabase(application).getCategoryDao()
        categoryRepository = CategoryRepository(categoryDao)
        categories = categoryRepository.getAll()

        val productDao = Database.getDatabase(application).getProductDao()
        productRepository = ProductRepository(productDao)
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