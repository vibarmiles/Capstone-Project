package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@Composable
fun PurchaseOrderScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, back: () -> Unit) {
    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.purchase_order), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO( adding of purchase orders )*/ }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
        paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(6) {
                 PurchaseOrderItem(supplier = "Supplier $it", date = LocalDate.now().toString(), totalCost = (it * 100).toDouble(), delivery = it % 2 == 1)
            }
        }
    }
}

@Composable
fun PurchaseOrderItem(supplier: String, date: String, totalCost: Double, delivery: Boolean) {
    androidx.compose.material3.ListItem(leadingContent = { Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Filled.Receipt, contentDescription = null) }, headlineContent = { Text(text = supplier, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = date) }, trailingContent = { Text(text = if (delivery) "Delivered" else "To Receive", color = if (delivery) Color.Green else Color.Red, fontWeight = FontWeight.Bold) })
}