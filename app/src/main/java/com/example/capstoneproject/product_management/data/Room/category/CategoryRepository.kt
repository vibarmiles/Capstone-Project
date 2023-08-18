package com.example.capstoneproject.product_management.data.Room.category

import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val dao: CategoryDao
) {
    fun getAll(): Flow<List<Category>> = dao.getAll()

    suspend fun insertCategory(category: Category) = dao.insertCategory(category = category)

    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category = category)
}