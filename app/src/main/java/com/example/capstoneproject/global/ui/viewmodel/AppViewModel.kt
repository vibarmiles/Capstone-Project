package com.example.capstoneproject.global.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.capstoneproject.global.data.firebase.ConnectionRepository

class AppViewModel : ViewModel() {
    var isLoading = mutableStateOf(true)
    private val connectionRepository: ConnectionRepository = ConnectionRepository()
    val connection = connectionRepository.connection
    val viewModelLoaded = mutableStateOf(false)
}