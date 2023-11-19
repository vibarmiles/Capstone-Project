package com.example.capstoneproject.supplier_management.ui.transfer_order

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Product

class TransferredProductsViewModel : ViewModel() {
    val transfers = mutableStateListOf<Product>()
}