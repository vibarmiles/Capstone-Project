package com.example.capstoneproject.product_management.ui.branch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.Database
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.data.Room.branch.BranchDao
import com.example.capstoneproject.product_management.data.Room.branch.BranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BranchViewModel(application: Application) : AndroidViewModel(application) {
    val branches: Flow<List<Branch>>
    private val repository: BranchRepository

    init {
        val branchDao: BranchDao = Database.getDatabase(application).getBranchDao()
        repository = BranchRepository(branchDao)
        branches = repository.getAll()
    }

    fun insert(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBranch(branch = branch)
        }
    }

    fun delete(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBranch(branch = branch)
        }
    }
}