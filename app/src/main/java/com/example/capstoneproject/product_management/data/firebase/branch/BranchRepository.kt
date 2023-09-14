package com.example.capstoneproject.product_management.data.firebase.branch

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class BranchRepository : IBranchRepository {
    private val firestore = Firebase.firestore
    private val branchCollectionReference = firestore.collection("branches")

    override fun getAll(): MutableLiveData<List<Branch>> {
        var branches = MutableLiveData<List<Branch>>()
        branchCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                 branches.value = value.toObjects()
            }
        }
        return branches
    }

    override fun insert(branch: Branch) {
        if (branch.id.isNotBlank()) {
            branchCollectionReference.document(branch.id).set(branch, SetOptions.merge())
        } else {
            branchCollectionReference.add(branch)
        }
    }

    override fun delete(branch: Branch) {
        branchCollectionReference.document(branch.id).delete()
    }
}