package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

@Composable
fun ViewPurchaseOrder(
    purchaseOrderId: String,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    dismissRequest: () -> Unit
) {
    val purchaseOrder = purchaseOrderViewModel.getDocument(id = purchaseOrderId)!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "View Purchase Order") },
                navigationIcon = {
                    IconButton(onClick = dismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
            paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            Text(text = "Date: ${purchaseOrder.date}")
        }
    }
}