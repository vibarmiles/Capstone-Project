package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.lifecycle.ViewModel
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Product

class PurchasedProductsViewModel : ViewModel() {
    val purchases: MutableList<Product> = mutableListOf()
}