package com.example.capstoneproject.product_management.data.Room.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.capstoneproject.product_management.data.Room.category.Category

@Entity(tableName = "Products")
data class Product(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "productId") val id: Int = 0,
    val image: String?,
    val productName: String,
    val price: Double,
    val category: Int = 0,
    val quantity: Int
)
