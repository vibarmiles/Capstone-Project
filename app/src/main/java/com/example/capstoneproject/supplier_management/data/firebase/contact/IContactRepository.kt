package com.example.capstoneproject.supplier_management.data.firebase.contact

import androidx.compose.runtime.snapshots.SnapshotStateMap

interface IContactRepository {
    fun getAll(): SnapshotStateMap<String, Contact>
    fun insert(key: String? = null, contact: Contact)
    fun addProductsForSupplier(key: String, product: Map<String, Double>)
    fun removeProductForSupplier(contactId: String, productId: String)
    fun delete(key: String)
}