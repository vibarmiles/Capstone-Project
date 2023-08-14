package com.example.capstoneproject.user_management.ui.add_users.composable

import android.app.Application
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.user_management.ui.viewmodel.UserViewModel
import com.example.capstoneproject.user_management.ui.viewmodel.UserViewModelFactory

@Composable
fun AddEditUserScreen(decision: String, back: () -> Unit) {
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
            UserManagementForm(back)
        }
    )
}