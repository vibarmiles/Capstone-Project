package com.example.capstoneproject.product_management.ui.product

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StackedBarChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import com.example.capstoneproject.product_management.data.firebase.product.Product


@Composable
fun ViewProduct(dismissRequest: () -> Unit, product: Product, edit: () -> Unit, set: () -> Unit, delete: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product.productName) },
                navigationIcon = {
                    IconButton(onClick = dismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    var expanded: Boolean by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Product") }, onClick = { expanded = false; edit.invoke() })
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.StackedBarChart, contentDescription = null) }, text = { Text(text = "Adjust Quantity") }, onClick = { expanded = false; set.invoke() })
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Product") }, onClick = { expanded = false; delete.invoke() })
                    }
                }
            )
        }
    ) {
            paddingValues -> paddingValues
    }
}