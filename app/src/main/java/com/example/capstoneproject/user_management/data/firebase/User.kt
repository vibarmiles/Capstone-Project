package com.example.capstoneproject.user_management.data.firebase

import com.google.firebase.firestore.DocumentId

data class User(
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val userLevel: UserLevel = UserLevel.Employee,
    val isActive: Boolean = true
)
