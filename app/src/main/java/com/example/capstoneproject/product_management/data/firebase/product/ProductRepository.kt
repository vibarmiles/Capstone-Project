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

class ProductRepository : IProductRepository {
    private val firebase = Firebase.database.reference
    private val productCollectionReference = firebase.child("products")
    private val firestorage = Firebase.storage.reference
    private val productImageReference = firestorage.child("images")

    override fun getAll(): SnapshotStateMap<String, Product> {
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

    override fun setQuantityForBranch(key: String, value: Map<String, Int>) {
        productCollectionReference.child(key).child("stock").setValue(value)
    }

    override fun insert(key: String?, product: Product) {
        val uri: Uri? = if (product.image != null) Uri.parse(product.image) else null

        if (key != null) {
            Log.d("ID is not blank", key)
            if (uri != null) {
                Log.d("Old Image", uri.lastPathSegment.toString())
                firestorage.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                    product.image = it.toString()
                    Log.d("Image", product.image.toString())
                    productCollectionReference.child(key).setValue(product)
                }.addOnFailureListener {
                    Log.d("Image", "File does not exist")
                    productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                        productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                            product.image = it.toString()
                            Log.d("Image", product.image.toString())
                            productCollectionReference.child(key).setValue(product)
                        }
                    }
                }
            } else {
                productCollectionReference.child(key).setValue(product)
            }
        } else {
            Log.d("ID is blank", product.toString())
            if (uri != null) {
                productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                    productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                        product.image = it.toString()
                        Log.d("Image", product.image.toString())
                        productCollectionReference.push().setValue(product)
                    }
                }
            } else {
                productCollectionReference.push().setValue(product)
            }
        }
    }

    override fun delete(key: String) {
        productCollectionReference.child(key).removeValue()
    }

    override fun removeCategory(categoryId: String) = productCollectionReference.orderByChild("category").equalTo(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (child in snapshot.children) {
                child.ref.child("category").removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })

    override fun removeBranchStock(branchId: String) = productCollectionReference.orderByChild("stock/$branchId").startAt(0.0).addListenerForSingleValueEvent(object : ValueEventListener {
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