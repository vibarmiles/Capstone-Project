package com.example.capstoneproject.user_management.ui.add_users.composable

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable

@Composable
fun AddEditUserScreen(decision: String, userId: String = "", back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "$decision User")
                },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        content = {
            it
            if (userId.isBlank()) {
                UserManagementForm(0, back)
            } else {
                UserManagementForm(userId.toInt(), back)
            }
        }
    )
}