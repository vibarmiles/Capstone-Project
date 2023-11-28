package com.example.capstoneproject.global.data.firebase.log

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Log(
    @DocumentId val id: String = "",
    val event: String = "",
    @ServerTimestamp val date: Date? = null,
    val userId: String = ""
)
