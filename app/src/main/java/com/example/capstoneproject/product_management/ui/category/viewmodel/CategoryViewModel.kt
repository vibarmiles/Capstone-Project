package com.example.capstoneproject.product_management.ui.category.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.Database
import com.example.capstoneproject.product_management.data.Room.category.Category
import com.example.capstoneproject.product_management.data.Room.category.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    val categories: Flow<List<Category>>
    private val repository: CategoryRepository

    init {
        val categoryDao = Database.getDatabase(application).getCategoryDao()
        repository = CategoryRepository(categoryDao)
        categories = repository.getAll()
    }

    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCategory(category = category)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category = category)
        }
    }
}