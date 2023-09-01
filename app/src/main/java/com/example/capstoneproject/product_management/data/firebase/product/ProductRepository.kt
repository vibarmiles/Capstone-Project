package com.example.capstoneproject.product_management.data.firebase.product

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firebase = Firebase.database.reference
    private val productCollectionReference = firebase.child("products")
    private val firestorage = Firebase.storage.reference
    private val productImageReference = firestorage.child("images")

    fun getAll(): SnapshotStateList<Product> {
        val products = mutableStateListOf<Product>()
        val productList: MutableList<Product> = products

        productCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                productList.add(snapshot.getValue<Product>()!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        return products
    }

    fun insert(product: Product) {
        if (product.id.isNotBlank()) {
            Log.d("ID is not blank", product.id)
            productCollectionReference.child(product.id).setValue(product)
        } else {
            Log.d("ID is blank", product.toString())
            val uri: Uri? = if (product.image != null) Uri.parse(product.image) else null
            if (uri != null) {
                productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                    productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                        product.image = it.toString()
                        productCollectionReference.push().setValue(product)
                    }
                }
            } else {
                productCollectionReference.push().setValue(product)
            }
        }
    }

    fun delete(product: Product) = productCollectionReference.child(product.id).removeValue()

}