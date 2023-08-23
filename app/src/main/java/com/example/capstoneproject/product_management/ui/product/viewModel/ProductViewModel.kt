package com.example.capstoneproject.product_management.ui.product.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.capstoneproject.global.data.Database
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.data.Room.branch.BranchRepository
import com.example.capstoneproject.product_management.data.Room.category.Category
import com.example.capstoneproject.product_management.data.Room.category.CategoryRepository
import kotlinx.coroutines.flow.Flow

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    val branches: Flow<List<Branch>>
    val categories: Flow<List<Category>>
    private val branchRepository: BranchRepository
    private val categoryRepository: CategoryRepository

    init {
        val branchDao = Database.getDatabase(application).getBranchDao()
        branchRepository = BranchRepository(branchDao)
        branches = branchRepository.getAll()

        val categoryDao = Database.getDatabase(application).getCategoryDao()
        categoryRepository = CategoryRepository(categoryDao)
        categories = categoryRepository.getAll()
    }
}