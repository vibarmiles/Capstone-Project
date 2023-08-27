package com.example.capstoneproject.product_management.data.Room.product

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.capstoneproject.product_management.data.Room.category.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT Products.*, Categories.id, Coalesce(Categories.categoryName, 'Default') as categoryName  FROM Products LEFT JOIN Categories ON Products.category = Categories.id ORDER BY Categories.categoryName, Products.productName ASC")
    fun getAll(): Flow<Map<Category, List<Product>>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product)

    @Delete
    fun deleteProduct(product: Product)
}