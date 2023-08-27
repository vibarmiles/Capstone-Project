package com.example.capstoneproject.user_management.data.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users")
data class User(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "userId") val id: Int = 0,
    val lastName: String,
    val firstName: String,
    val email: String,
    val password: String,
    val salt: String
)
