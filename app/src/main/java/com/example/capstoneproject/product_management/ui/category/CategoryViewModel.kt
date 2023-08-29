package com.example.capstoneproject.product_management.ui.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.category.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    val categories: MutableLiveData<List<Category>>
    private val categoryRepository: CategoryRepository = CategoryRepository()

    init {
        categories = categoryRepository.getAll()
    }

    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.insert(category = category)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.delete(category = category)
        }
    }
}