package com.example.capstoneproject.product_management.ui.category

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.category.CategoryRepository
import com.example.capstoneproject.product_management.data.firebase.category.ICategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private lateinit var categories: MutableLiveData<List<Category>>
    private val categoryRepository: ICategoryRepository = CategoryRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<Category>> {
        if (!this::categories.isInitialized) {
            categories = categoryRepository.getAll(callback = { updateLoadingState() }) { result ->
                resultState.update { result }
            }
        }

        return categories
    }

    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.insert(category = category) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.delete(category = category) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }
}