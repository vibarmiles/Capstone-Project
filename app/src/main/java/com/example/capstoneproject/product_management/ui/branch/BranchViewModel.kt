package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    var isLoading: MutableState<Boolean> = mutableStateOf(true)

    fun getAll(): MutableLiveData<List<Branch>> {
        if (!this::branches.isInitialized) {
            branches = branchRepository.getAll { updateLoadingState() }
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

    private fun updateLoadingState() {
        isLoading.value = isLoading.value.not()
    }
}