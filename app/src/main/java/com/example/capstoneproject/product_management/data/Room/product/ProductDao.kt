package com.example.capstoneproject.product_management.data.Room.product

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM Products")
    fun getAll(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product)

    @Delete
    fun deleteProduct(product: Product)
}