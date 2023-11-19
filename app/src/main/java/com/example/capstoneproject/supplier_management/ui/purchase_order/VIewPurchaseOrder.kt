package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.ui.Document
import com.example.capstoneproject.supplier_management.ui.DocumentDialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ViewPurchaseOrder(
    purchaseOrderId: String,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    productViewModel: ProductViewModel,
    branchViewModel: BranchViewModel,
    dismissRequest: () -> Unit
) {
    val purchaseOrder = purchaseOrderViewModel.getDocument(id = purchaseOrderId)!!
    val products = remember { purchaseOrder.products.map { (productViewModel.getProduct(it.value.id)?.productName ?: "Unknown Item") to it.value } }
    var showDialog by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf("") }
    var action: Status? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "View ${Document.PO.doc}") },
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
            .padding(16.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "ID: ${purchaseOrder.id}")
                        Text(text = "Date: ${purchaseOrder.date}")
                        Text(text = "Status: ${purchaseOrder.status}")
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

            if (purchaseOrder.status == Status.WAITING) {
                var expanded by remember { mutableStateOf(false) }
                val branches = branchViewModel.getAll().observeAsState(listOf())
                var selected by remember { mutableStateOf(branches.value.firstOrNull()?.name ?: "No Branches Entered") }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = selected, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.branch)) })

                    DropdownMenu(modifier = Modifier
                        .exposedDropdownSize()
                        .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {

                        branches.value.forEach {
                            androidx.compose.material3.DropdownMenuItem(text = { Text(text = it.name) }, onClick = { id = it.id; selected = it.name; expanded = false })
                        }
                    }
                }

                if (branches.value.isNotEmpty()) {
                    if (id.isBlank()) {
                        id = branches.value.first().id
                    }

                    FormButtons(submitText = "Received", cancel = {
                        action = Status.CANCELLED
                        showDialog = true
                    }) {
                        action = Status.COMPLETE
                        showDialog = true
                    }
                }
            }

            if (showDialog && action != null) {
                DocumentDialog(action = action!!.name, type = Document.PO, onCancel = { showDialog = false }) {
                    if (action == Status.COMPLETE) {
                        purchaseOrder.products.forEach {
                            productViewModel.getProduct(id = it.value.id)?.let {
                                    product ->
                                productViewModel.insert(id = it.value.id, product = run {
                                    val stock = product.stock.toMutableMap()
                                    stock[id] = (stock[id] ?: 0) + it.value.quantity
                                    product.copy(stock = stock, transaction = product.transaction.let { transaction -> transaction.copy(purchased = transaction.purchased + it.value.quantity) })
                                })
                            }
                        }
                    }

                    purchaseOrderViewModel.insert(purchaseOrder = purchaseOrder.copy(status = action!!))
                    dismissRequest.invoke()
                }
            }
        }
    }
}