package com.example.capstoneproject.point_of_sales.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.capstoneproject.point_of_sales.data.firebase.Product

class SoldProductsViewModel : ViewModel() {
    val sales = mutableStateListOf<Product>()
}