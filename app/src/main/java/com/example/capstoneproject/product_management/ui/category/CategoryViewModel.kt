package com.example.capstoneproject.product_management.ui.category

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.category.CategoryRepository
import com.example.capstoneproject.product_management.data.firebase.category.ICategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private lateinit var categories: MutableLiveData<List<Category>>
    private val categoryRepository: ICategoryRepository = CategoryRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)

    fun getAll(): MutableLiveData<List<Category>> {
        if (!this::categories.isInitialized) {
            categories = categoryRepository.getAll { updateLoadingState() }
        }

        return categories
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

    private fun updateLoadingState() {
        isLoading.value = isLoading.value.not()
    }
}