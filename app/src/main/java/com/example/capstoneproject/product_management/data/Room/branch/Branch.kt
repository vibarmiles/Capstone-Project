package com.example.capstoneproject.product_management.data.Room.branch

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Branches")
data class Branch(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "branchId") val id: Int = 0,
    val branchName: String,
    val address: String
)
