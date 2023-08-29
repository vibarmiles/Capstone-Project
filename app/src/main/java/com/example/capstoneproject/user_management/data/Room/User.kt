package com.example.capstoneproject.user_management.data.Room

data class User(
    val id: Int = 0,
    val lastName: String,
    val firstName: String,
    val email: String,
    val password: String,
    val salt: String
)
