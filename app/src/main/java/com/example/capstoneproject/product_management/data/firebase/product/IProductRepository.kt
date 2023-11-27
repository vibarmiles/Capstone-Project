package com.example.capstoneproject.product_management.data.firebase.product

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice

interface IProductRepository {
    fun getAll(callback: () -> Unit, update: () -> Unit, result: (FirebaseResult) -> Unit): SnapshotStateMap<String, Product>
    fun setQuantityForBranch(key: String, value: Map<String, Int>, result: (FirebaseResult) -> Unit)
    fun insert(key: String? = null, product: Product, result: (FirebaseResult) -> Unit)
    fun delete(key: String, product: Product, result: (FirebaseResult) -> Unit)
    fun removeCategory(categoryId: String, result: (FirebaseResult) -> Unit)
    fun removeBranchStock(branchId: String, result: (FirebaseResult) -> Unit)
    fun transact(document: Any, result: (FirebaseResult) -> Unit)
}