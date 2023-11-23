package com.example.capstoneproject.supplier_management.ui.transfer_order

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
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.ui.Document
import com.example.capstoneproject.supplier_management.ui.DocumentDialog

@Composable
fun ViewTransferOrder(
    transferOrderId: String,
    transferOrderViewModel: TransferOrderViewModel,
    productViewModel: ProductViewModel,
    branchViewModel: BranchViewModel,
    dismissRequest: () -> Unit
) {
    val transferOrder = transferOrderViewModel.getDocument(id = transferOrderId)!!
    val oldBranch = branchViewModel.getBranch(transferOrder.oldBranchId)
    val destinationBranch = branchViewModel.getBranch(transferOrder.destinationBranchId)
    val products = remember { transferOrder.products.map { (productViewModel.getProduct(it.value.id)?.productName ?: "Unknown Item") to it.value } }
    var showDialog by remember { mutableStateOf(false) }
    var action: Status? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("View ${Document.RO.doc}").uppercase()) },
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
                        Text(text = "ID: ${transferOrder.id}")
                        Text(text = "Date: ${transferOrder.date}")
                        Text(text = "Old Branch: ${oldBranch?.name ?: "Missing Branch"}")
                        Text(text = "Destination Branch: ${destinationBranch?.name ?: "Missing Branch"}")
                        Text(text = "Status: ${transferOrder.status}")
                        Divider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Item", modifier = Modifier.weight(1f))
                            Text(text = "Qty", modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                        }
                        Divider()
                    }
                }

                items(products) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = it.first, modifier = Modifier.weight(1f))
                        Text(text = it.second.quantity.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                    }
                }
            }

            if (transferOrder.status == Status.WAITING) {
                FormButtons(submitText = "Transferred", cancel = {
                    action = Status.CANCELLED
                    showDialog = true
                }) {
                    action = Status.COMPLETE
                    showDialog = true
                }
            }

            if (showDialog && action != null) {
                DocumentDialog(action = action!!, type = Document.TO, onCancel = { showDialog = false }) {
                    if (action == Status.COMPLETE) {
                        transferOrder.products.forEach {
                            productViewModel.getProduct(id = it.value.id)?.let {
                                    product ->
                                productViewModel.insert(id = it.value.id, product = run {
                                    val stock = product.stock.toMutableMap()
                                    stock[transferOrder.oldBranchId]?.let { quantity -> stock[transferOrder.oldBranchId] = if (quantity > it.value.quantity) quantity - it.value.quantity else 0 }
                                    stock[transferOrder.destinationBranchId]?.let { quantity -> stock[transferOrder.destinationBranchId] = quantity + it.value.quantity }
                                    product.copy(stock = stock)
                                })
                            }
                        }
                    }

                    transferOrderViewModel.insert(transferOrder = transferOrder.copy(status = action!!))
                    dismissRequest.invoke()
                }
            }
        }
    }
}