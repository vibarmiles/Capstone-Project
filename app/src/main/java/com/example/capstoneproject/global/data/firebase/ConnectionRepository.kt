package com.example.capstoneproject.global.data.firebase

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ConnectionRepository {
    private val connectionReference = Firebase.database.getReference(".info/connected")
    val connection = mutableStateOf(true)

    init {
        connectionReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                connection.value = snapshot.getValue<Boolean>() ?: false
                Log.d("Connection", connection.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}