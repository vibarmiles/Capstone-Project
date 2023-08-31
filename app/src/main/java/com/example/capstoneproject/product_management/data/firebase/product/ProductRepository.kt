package com.example.capstoneproject.product_management.data.firebase.product

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ProductRepository {
    private val firebase = Firebase.database.reference
    private val productCollectionReference = firebase.child("products")

    fun getAll(): MutableLiveData<List<Product>> {
        val products: MutableLiveData<List<Product>> = MutableLiveData<List<Product>>()
        val productList = mutableListOf<Product>()

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
        products.value = productList
        return products
    }

    fun insert(product: Product) = if (product.id.isNotBlank()) { Log.d("ID is not blank", product.id); productCollectionReference.child(product.id).setValue(product) } else { Log.d("ID is blank", product.toString()); productCollectionReference.push().setValue(product) }

    fun delete(product: Product) = productCollectionReference.child(product.id).removeValue()
}