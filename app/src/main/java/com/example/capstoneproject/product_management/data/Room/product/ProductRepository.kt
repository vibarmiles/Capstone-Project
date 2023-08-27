package com.example.capstoneproject.product_management.data.Room.product

import com.example.capstoneproject.product_management.data.Room.category.Category
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao
) {
    fun getAll(): Flow<Map<Category, List<Product>>> = dao.getAll()

    suspend fun insert(product: Product) = dao.insertProduct(product = product)

    suspend fun delete(product: Product) = dao.deleteProduct(product = product)
}