package com.example.capstoneproject.user_management.data.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue

data class User(
    @Exclude val id: String? = null,
    val lastName: String = "",
    val firstName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String? = null,
    val firstLogin: Boolean = true,
    val lastLogin: Any = ServerValue.TIMESTAMP,
    val userLevel: UserLevel = UserLevel.Cashier,
    val branchId: String? = null,
    val active: Boolean = true
)
