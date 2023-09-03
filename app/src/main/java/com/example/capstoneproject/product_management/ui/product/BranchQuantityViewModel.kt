package com.example.capstoneproject.product_management.ui.product

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel

class BranchQuantityViewModel : ViewModel() {
    var stockPerBranch: MutableMap<String, String> = mutableMapOf()
    val checkInput = mutableStateMapOf<String, Boolean>()
}