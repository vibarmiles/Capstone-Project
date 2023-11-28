package com.example.capstoneproject.user_management.data.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.DocumentId

data class User(
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val lastLogin: Any = ServerValue.TIMESTAMP,
    val userLevel: UserLevel = UserLevel.Employee,
    val branchId: String? = null,
    val isActive: Boolean = true
)
