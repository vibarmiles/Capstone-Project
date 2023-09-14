package com.example.capstoneproject.product_management.data.firebase.category

import androidx.lifecycle.MutableLiveData

interface ICategoryRepository {
    fun getAll(): MutableLiveData<List<Category>>
    fun insert(category: Category)
    fun delete(category: Category)
}