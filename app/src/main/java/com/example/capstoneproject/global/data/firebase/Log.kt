package com.example.capstoneproject.global.data.firebase

import com.google.firebase.firestore.DocumentId

data class Log(
    @DocumentId val id: String = "",
    val event: String = "",
    val date: String = "",
    val userId: String = ""
)
