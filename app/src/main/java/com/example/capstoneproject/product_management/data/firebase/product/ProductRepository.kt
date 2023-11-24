package com.example.capstoneproject.product_management.data.firebase.product

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProductRepository : IProductRepository {
    private val firebase = Firebase.database.reference
    private val productCollectionReference = firebase.child("products")
    private val firestorage = Firebase.storage.reference
    private val productImageReference = firestorage.child("images")

    override fun getAll(callback: () -> Unit, update: () -> Unit, result: (FirebaseResult) -> Unit): SnapshotStateMap<String, Product> {
        val products = mutableStateMapOf<String, Product>()

        productCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
                Log.d("Added", snapshot.value.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
                update.invoke()
                Log.d("Updated", snapshot.value.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                products.remove(snapshot.key)
                update.invoke()
                Log.d("Removed", snapshot.value.toString())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
            }

        })

        productCollectionReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
            }
        })

        return products
    }

    override fun setQuantityForBranch(key: String, value: Map<String, Int>, result: (FirebaseResult) -> Unit) {
        productCollectionReference.child(key).child("stock").setValue(value).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }

    override fun insert(key: String?, product: Product, result: (FirebaseResult) -> Unit) {
        val uri: Uri? = if (product.image != null) Uri.parse(product.image) else null

        if (key != null) {
            Log.d("ID is not blank", key)
            if (uri != null) {
                Log.d("Old Image", uri.lastPathSegment.toString())
                firestorage.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                    product.image = it.toString()
                    Log.d("Image", product.image.toString())
                    productCollectionReference.child(key).setValue(product).addOnSuccessListener {
                        result.invoke(FirebaseResult(result = true))
                    }.addOnFailureListener {
                            e ->
                        result.invoke(FirebaseResult(result = false, errorMessage = e.message))
                    }
                }.addOnFailureListener {
                    Log.d("Image", "File does not exist")
                    productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                        productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                            product.image = it.toString()
                            Log.d("Image", product.image.toString())
                            productCollectionReference.child(key).setValue(product).addOnSuccessListener {
                                result.invoke(FirebaseResult(result = true))
                            }.addOnFailureListener {
                                    e ->
                                result.invoke(FirebaseResult(result = false, errorMessage = e.message))
                            }
                        }
                    }
                }
            } else {
                productCollectionReference.child(key).setValue(product).addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        } else {
            Log.d("ID is blank", product.toString())
            if (uri != null) {
                productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                    productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                        product.image = it.toString()
                        Log.d("Image", product.image.toString())
                        productCollectionReference.push().setValue(product).addOnSuccessListener {
                            result.invoke(FirebaseResult(result = true))
                        }.addOnFailureListener {
                            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                        }
                    }
                }
            } else {
                productCollectionReference.push().setValue(product).addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        }
    }

    override fun delete(key: String, result: (FirebaseResult) -> Unit) {
        productCollectionReference.child(key).removeValue().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }

    override fun removeCategory(categoryId: String, result: (FirebaseResult) -> Unit) = productCollectionReference.orderByChild("category").equalTo(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (child in snapshot.children) {
                child.ref.child("category").removeValue().addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            result.invoke(FirebaseResult(result = false, errorMessage = error.message))
        }

    })

    override fun removeBranchStock(branchId: String, result: (FirebaseResult) -> Unit) = productCollectionReference.orderByChild("stock/$branchId").startAt(0.0).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d("Calling", "Remove Branch Stock")
            for (child in snapshot.children) {
                child.ref.child("stock/$branchId").removeValue()
            }

            result.invoke(FirebaseResult(result = true))
        }

        override fun onCancelled(error: DatabaseError) {
            result.invoke(FirebaseResult(result = false, errorMessage = error.message))
        }

    })
}