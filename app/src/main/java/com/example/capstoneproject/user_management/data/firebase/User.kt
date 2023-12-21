package com.example.capstoneproject.user_management.data.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue

data class User(
    @Exclude val id: String? = null,
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val lastLogin: Any = ServerValue.TIMESTAMP,
    val userLevel: UserLevel = UserLevel.Employee,
    val branchId: String? = null,
    val active: Boolean = true
)
