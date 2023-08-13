package com.example.capstoneproject.user_management.domain.model.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val id: Int? = null,
    val last_name: String,
    val first_name: String,
    val email: String,
    val password: String,
    val salt: String
)
