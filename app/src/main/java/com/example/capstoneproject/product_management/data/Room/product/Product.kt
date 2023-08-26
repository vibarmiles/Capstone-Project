package com.example.capstoneproject.product_management.data.Room.product

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val image: String,
    val productName: String,
    val price: Double,
    val category: Int = 0,
    val quantity: Int
)
