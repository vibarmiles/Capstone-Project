package com.example.capstoneproject.product_management.data.firebase.product

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProductRepository {
    private val firebase = Firebase.database.reference
    private val productCollectionReference = firebase.child("products")
    private val firestorage = Firebase.storage.reference
    private val productImageReference = firestorage.child("images")

    fun getAll(): SnapshotStateMap<String, Product> {
        val products = mutableStateMapOf<String, Product>()
        val productList: MutableMap<String, Product> = products

        productCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                productList[snapshot.key!!] = snapshot.getValue<Product>()!!
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                productList[snapshot.key!!] = snapshot.getValue<Product>()!!
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                productList.remove(snapshot.key)
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

    fun insert(key: String? = null, product: Product) {
        if (key != null) {
            Log.d("ID is not blank", key)
            productCollectionReference.child(key).setValue(product)
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

    fun delete(key: String) = productCollectionReference.child(key).removeValue()

}