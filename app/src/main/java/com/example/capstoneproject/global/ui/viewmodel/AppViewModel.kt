package com.example.capstoneproject.global.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    var isLoading = mutableStateOf(true)
}