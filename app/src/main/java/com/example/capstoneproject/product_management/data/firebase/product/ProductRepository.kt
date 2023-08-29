package com.example.capstoneproject.product_management.data.firebase.product

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class ProductRepository {
    private val firestore = Firebase.firestore
    private val productCollectionReference = firestore.collection("products")
    private val categoryCollectionReference = firestore.collection("categories")

    fun getAll(): MutableLiveData<Map<Category, List<Product>>> {
        var products = MutableLiveData<Map<Category, List<Product>>>()
        var productMap = mutableMapOf<Category, List<Product>>()

        categoryCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                it.forEach {
                    category ->
                    productCollectionReference.addSnapshotListener { value, error ->
                        error?.let {
                            return@addSnapshotListener
                        }
                        value?.let {
                            productMap.put(category.toObject(), value.toObjects())
                        }
                    }
                }
            }
        }
        products.value = productMap
        return products
    }

    fun insert(product: Product) = if (product.id.isNotBlank()) { productCollectionReference.document(product.id).set(product, SetOptions.merge()) } else { productCollectionReference.add(product) }

    fun delete(product: Product) = productCollectionReference.document(product.id).delete()
}