package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Product
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PurchaseOrderScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, add: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var noOfDaysShown by remember { mutableStateOf(0) }
    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.purchase_order), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
        paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)) {
            ExposedDropdownMenuBox(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp), expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                var textFieldValue by remember { mutableStateOf("Today") }
                val dropdownMenuItems = listOf("Today", "Last 3 days", "Last 7 days", "Last 30 days")
                OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = textFieldValue, readOnly = true, onValueChange = {  })
                DropdownMenu(modifier = Modifier
                    .exposedDropdownSize()
                    .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                    dropdownMenuItems.forEachIndexed {
                            index, s ->
                        androidx.compose.material3.DropdownMenuItem(text = { androidx.compose.material.Text(text = s) }, onClick = { noOfDaysShown = index; textFieldValue = s; expanded = false })
                    }
                }
            }
            LazyColumn {
                items(6) {
                    PurchaseOrderItem(PurchaseOrder(supplier = "Supplier $it", date = LocalDate.now().toString(), status = it % 2 == 1, products = listOf(Product(name = "Product $it", quantity = 2, price = 3.23)))) {

                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
fun PurchaseOrderItem(purchaseOrder: PurchaseOrder, goto: (String) -> Unit) {
    androidx.compose.material3.ListItem(
        modifier = Modifier.clickable { goto.invoke(purchaseOrder.id) },
        overlineContent = { Text(text = purchaseOrder.supplier, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) },
        headlineContent = { Text(text = purchaseOrder.date) },
        supportingContent = { Text(text = "Number of Items: ${purchaseOrder.products.count()}") },
        trailingContent = { 
            Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                Text(text = "${purchaseOrder.products.sumOf { (it.price * it.quantity) }} PHP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = if (purchaseOrder.status) "Delivered" else "To Receive", fontSize = 12.sp, color = if (purchaseOrder.status) Color.Green else Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    )
    Divider()
}