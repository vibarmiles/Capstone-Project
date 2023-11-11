package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.branch.BranchRepository
import com.example.capstoneproject.product_management.data.firebase.branch.IBranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BranchViewModel : ViewModel() {
    private lateinit var branches: MutableLiveData<List<Branch>>
    private val branchRepository: IBranchRepository = BranchRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<Branch>> {
        if (!this::branches.isInitialized) {
            branches = branchRepository.getAll(callback = { updateLoadingState() }) {
                    result ->
                resultState.update { result }
            }

        }

        return branches
    }

    fun insert(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            branchRepository.insert(branch = branch) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            branchRepository.delete(branch = branch) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getBranch(id: String?): Branch? {
        return branches.value?.firstOrNull { branch -> branch.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }
}