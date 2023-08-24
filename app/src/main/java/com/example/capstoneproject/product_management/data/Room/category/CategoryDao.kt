package com.example.capstoneproject.product_management.data.Room.category

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Categories")
    fun getAll(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)
}