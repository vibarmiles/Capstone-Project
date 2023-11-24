package com.example.capstoneproject.product_management.data.firebase.category

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class CategoryRepository : ICategoryRepository {
    private val firestore = Firebase.firestore
    private val categoryCollectionReference = firestore.collection("categories")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Category>> {
        val categories = MutableLiveData<List<Category>>()
        categoryCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                categories.value = value.toObjects()
                callback.invoke()
            }
        }
        return categories
    }

    override fun insert(category: Category, result: (FirebaseResult) -> Unit) {
        if (category.id.isNotBlank()) {
            categoryCollectionReference.document(category.id).set(category, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            categoryCollectionReference.add(category).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(category: Category, result: (FirebaseResult) -> Unit) {
        categoryCollectionReference.document(category.id).delete().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}