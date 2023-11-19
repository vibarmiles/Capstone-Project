package com.example.capstoneproject.supplier_management.ui.return_order

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Product

class ReturnedProductsViewModel : ViewModel() {
    val returns = mutableStateListOf<Product>()
}