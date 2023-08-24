package com.example.capstoneproject.product_management.data.Room.product

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao
) {
    fun getAll(): Flow<List<Product>> = dao.getAll()

    suspend fun insert(product: Product) = dao.insertProduct(product = product)

    suspend fun delete(product: Product) = dao.deleteProduct(product = product)
}