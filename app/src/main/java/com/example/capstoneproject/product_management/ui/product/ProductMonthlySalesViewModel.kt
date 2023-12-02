package com.example.capstoneproject.product_management.ui.product

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import java.time.Month

class ProductMonthlySalesViewModel : ViewModel() {
    var salesPerMonth: MutableMap<Month, String> = mutableMapOf()
    val checkInput = mutableStateMapOf<Month, Boolean>()
}