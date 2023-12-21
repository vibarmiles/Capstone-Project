package com.example.capstoneproject.product_management.data.firebase.category

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface ICategoryRepository {
    fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Category>>
    fun insert(category: Category, result: (FirebaseResult) -> Unit)
    fun delete(category: Category, result: (FirebaseResult) -> Unit)
    fun archiveItem(category: Category, result: (FirebaseResult) -> Unit)
}