package com.example.capstoneproject.product_management.data.firebase.product

import androidx.compose.runtime.snapshots.SnapshotStateMap

interface IProductRepository {
    fun getAll(callback: () -> Unit, update: () -> Unit): SnapshotStateMap<String, Product>
    fun setQuantityForBranch(key: String, value: Map<String, Int>)
    fun insert(key: String? = null, product: Product)
    fun delete(key: String)
    fun removeCategory(categoryId: String)
    fun removeBranchStock(branchId: String)
}