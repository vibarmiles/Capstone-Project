package com.example.capstoneproject.global.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.capstoneproject.global.data.firebase.ConnectionRepository
import com.example.capstoneproject.login.data.login.SignInResult

class AppViewModel : ViewModel() {
    var isLoading = mutableStateOf(true)
    private val connectionRepository: ConnectionRepository = ConnectionRepository()
    val connection = connectionRepository.connection
    val user = mutableStateOf(SignInResult(data = null, errorMessage = null))
}