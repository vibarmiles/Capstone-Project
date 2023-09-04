package com.example.capstoneproject.product_management.data.firebase.product

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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

        productCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
                Log.d("Added", snapshot.value.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
                Log.d("Updated", snapshot.value.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                products.remove(snapshot.key)
                Log.d("Removed", snapshot.value.toString())
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

    fun setQuantityForBranch(key: String, value: Map<String, Int>) = productCollectionReference.child(key).child("stock").setValue(value)

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

    fun removeCategory(categoryId: String) = productCollectionReference.orderByChild("category").equalTo(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (child in snapshot.children) {
                child.ref.child("category").removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })

    fun removeBranchStock(branchId: String) = productCollectionReference.orderByChild("stock/$branchId").startAt(0.0).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d("Calling", "Remove Branch Stock")
            for (child in snapshot.children) {
                child.ref.child("stock/$branchId").removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })
}