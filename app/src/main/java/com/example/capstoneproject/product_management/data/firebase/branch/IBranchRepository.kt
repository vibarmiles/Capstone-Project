package com.example.capstoneproject.product_management.data.firebase.branch

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface IBranchRepository {
    fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Branch>>
    fun insert(branch: Branch, result: (FirebaseResult) -> Unit)
    fun delete(branch: Branch, result: (FirebaseResult) -> Unit)
    fun archiveItem(branch: Branch, result: (FirebaseResult) -> Unit)
}