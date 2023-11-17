package com.example.capstoneproject.supplier_management.ui.return_order

import androidx.lifecycle.ViewModel
import com.example.capstoneproject.supplier_management.data.firebase.return_order.Product

class ReturnedProductsViewModel : ViewModel() {
    val returns: MutableList<Product> = mutableListOf()
}