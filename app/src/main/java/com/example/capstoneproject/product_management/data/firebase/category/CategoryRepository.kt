package com.example.capstoneproject.product_management.data.firebase.category

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class CategoryRepository {
    private val firestore = Firebase.firestore
    private val categoryCollectionReference = firestore.collection("categories")

    fun getAll(): MutableLiveData<List<Category>> {
        var categories = MutableLiveData<List<Category>>()
        categoryCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                categories.value = value.toObjects()
            }
        }
        return categories
    }

    fun insert(category: Category) = if (category.id.isNotBlank()) { categoryCollectionReference.document(category.id).set(category, SetOptions.merge()) } else { categoryCollectionReference.add(category) }

    fun delete(category: Category) = categoryCollectionReference.document(category.id).delete()
}