package com.example.capstoneproject.login.data.login

data class SignInResult(
    val data: User?,
    val errorMessage: String?
)

data class User(
    val id: String,
    val username: String,
    val profilePicture: String?,
    val email: String
)
