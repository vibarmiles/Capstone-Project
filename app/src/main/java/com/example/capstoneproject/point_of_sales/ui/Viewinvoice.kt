package com.example.capstoneproject.point_of_sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.ui.Document
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@Composable
fun ViewInvoice(
    invoiceId: String,
    posViewModel: POSViewModel,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    branchViewModel: BranchViewModel,
    dismissRequest: () -> Unit
) {
    val invoice = posViewModel.getDocument(id = invoiceId)!!
    val products = remember { invoice.products.map { (productViewModel.getProduct(it.value.id)?.productName ?: "Unknown Item") to it.value } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("View Sales Invoice").uppercase()) },
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
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "ID: ${invoice.id}")
                        Text(text = "Date: ${invoice.date}")
                        Text(text = "Branch: ${branchViewModel.getBranch(id = invoice.branchId)?.name}")
                        Text(text = "Employee: ${userViewModel.getUserDetails(id = invoice.userId)?.let { it.lastName + ", " + it.firstName }}")
                        Text(text = "Employee ID: ${invoice.userId}")
                        Divider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Item", modifier = Modifier.weight(1f))
                            Text(text = "Price", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text(text = "Qty", modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                            Text(text = "Total", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                        Divider()
                    }
                }

                items(products) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = it.first, modifier = Modifier.weight(1f))
                        Text(text = it.second.price.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text(text = it.second.quantity.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                        Text(text = (it.second.price * it.second.quantity).toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                }

                item {
                    Divider()
                    Row {
                        Text(text = "Total Price:", modifier = Modifier.weight(1f))
                        Text(text = products.sumOf { it.second.price * it.second.quantity }.toString())
                    }
                }
            }
        }
    }
}