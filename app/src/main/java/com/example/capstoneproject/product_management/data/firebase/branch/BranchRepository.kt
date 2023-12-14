package com.example.capstoneproject.product_management.data.firebase.branch

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class BranchRepository : IBranchRepository {
    private val firestore = Firebase.firestore
    private val branchCollectionReference = firestore.collection("branches")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Branch>> {
        val branches = MutableLiveData<List<Branch>>()
        branchCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                branches.value = value.toObjects()
                callback.invoke()
            }
        }
        return branches
    }

    override fun insert(branch: Branch, result: (FirebaseResult) -> Unit) {
        if (branch.id.isNotBlank()) {
            branchCollectionReference.document(branch.id).set(branch, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            branchCollectionReference.add(branch).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(branch: Branch, result: (FirebaseResult) -> Unit) {
        branchCollectionReference.document(branch.id).set(branch.copy(active = branch.active.not())).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}