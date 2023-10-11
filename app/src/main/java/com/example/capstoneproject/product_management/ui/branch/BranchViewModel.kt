package com.example.capstoneproject.product_management.ui.branch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.branch.BranchRepository
import com.example.capstoneproject.product_management.data.firebase.branch.IBranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BranchViewModel : ViewModel() {
    private lateinit var branches: MutableLiveData<List<Branch>>
    private val branchRepository: IBranchRepository = BranchRepository()

    fun getAll(): MutableLiveData<List<Branch>> {
        if (!this::branches.isInitialized) {
            branches = branchRepository.getAll()
        }

        return branches
    }

    fun insert(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            branchRepository.insert(branch = branch)
        }
    }

    fun delete(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            branchRepository.delete(branch = branch)
        }
    }
}