package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact

@Composable
fun TurnoverRatio(
    products: List<Pair<Double, Product>>,
    suppliers: List<Contact>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = products) {
            Column {
                ListItem(
                    colors = ProjectListItemColors(),
                    headlineContent = {
                        Text(
                            text = it.second.productName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Text(text = suppliers.firstOrNull { supplier -> supplier.id == it.second.supplier }?.name ?: "Unknown Supplier")
                    },
                    trailingContent = {
                        Text(
                            text = String.format("%.2f", it.first),
                            color = if (it.first < 1) MaterialTheme.colors.error else MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Divider()
            }
        }
    }
}