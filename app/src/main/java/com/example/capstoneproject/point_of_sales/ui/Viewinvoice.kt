package com.example.capstoneproject.point_of_sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.KeyboardReturn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.ui.Document
import com.example.capstoneproject.supplier_management.ui.DocumentDialog
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@Composable
fun ViewInvoice(
    invoiceId: String,
    posViewModel: POSViewModel,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    branchViewModel: BranchViewModel,
    returnAndExchange: () -> Unit,
    dismissRequest: () -> Unit,
) {
    val invoice = posViewModel.getDocument(id = invoiceId)!!
    val products = remember { invoice.products.map { (productViewModel.getProduct(it.value.id)?.productName ?: "Unknown Item") to it.value } }
    val confirm = remember { mutableStateOf(true) }
    var action: Status? = null
    var showDialog by remember { mutableStateOf(false) }
    val state = posViewModel.result.collectAsState()

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
                },
                actions = {
                    if (invoice.invoiceType != InvoiceType.REFUND) {
                        var expanded by remember { mutableStateOf(false) }

                        IconButton(onClick = { expanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            androidx.compose.material3.DropdownMenuItem(
                                leadingIcon = { Icon(imageVector = Icons.Outlined.KeyboardReturn, contentDescription = null) },
                                text = { Text(text = "Issue Return And Exchange") },
                                onClick = {
                                    expanded = false
                                    returnAndExchange.invoke()
                                }
                            )
                        }
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
                        Text(text = "Employee: ${userViewModel.getUserDetails(userId = invoice.userId)?.let { it.lastName + ", " + it.firstName }}")
                        Text(text = "Employee ID: ${invoice.userId}")
                        Text(text = "Payment Type: ${invoice.payment}")
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
                        Text(text = "Subtotal:", modifier = Modifier.weight(1f))
                        Text(text = products.sumOf { it.second.price * it.second.quantity }.toString())
                    }
                }

                item {
                    Divider()
                    Row {
                        Text(text = "Discount:", modifier = Modifier.weight(1f))
                        Text(text = invoice.discount.toString())
                    }
                }

                item {
                    Divider()
                    Row {
                        Text(text = "Total Price:", modifier = Modifier.weight(1f))
                        Text(text = (products.sumOf { it.second.price * it.second.quantity } - invoice.discount).toString())
                    }
                }
            }

            if (showDialog && action != null) {
                DocumentDialog(action = action!!, type = Document.RO, onCancel = { showDialog = false }) {
                    posViewModel.transactFromWaiting(document = invoice.copy(status = action!!))
                    showDialog = false
                    confirm.value = false
                }
            }

            if (invoice.status == Status.WAITING && confirm.value) {
                FormButtons(submitText = "Complete", cancel = {
                    action = Status.CANCELLED
                    showDialog = true
                }) {
                    action = Status.COMPLETE
                    showDialog = true
                }
            }

            LaunchedEffect(key1 = state.value) {
                if (state.value.result) {
                    userViewModel.log(event = "${if (action == Status.COMPLETE) "complete" else "cancel"}_sales_invoice")
                    dismissRequest.invoke()
                } else if (!state.value.result && state.value.errorMessage != null) {
                    userViewModel.log(event = "fail_sales_invoice")
                    dismissRequest.invoke()
                }
            }
        }
    }
}