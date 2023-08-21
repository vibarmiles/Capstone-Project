package com.example.capstoneproject.product_management.data.Room.branch

import kotlinx.coroutines.flow.Flow

class BranchRepository(
    private val dao: BranchDao
) {
    fun getAll(): Flow<List<Branch>> = dao.getAll()

    suspend fun insertBranch(branch: Branch) = dao.insertBranch(branch = branch)

    suspend fun deleteBranch(branch: Branch) = dao.deleteBranch(branch = branch)
}