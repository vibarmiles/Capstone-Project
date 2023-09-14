package com.example.capstoneproject.product_management.data.firebase.branch

import androidx.lifecycle.MutableLiveData

interface IBranchRepository {
    fun getAll(): MutableLiveData<List<Branch>>
    fun insert(branch: Branch)
    fun delete(branch: Branch)
}