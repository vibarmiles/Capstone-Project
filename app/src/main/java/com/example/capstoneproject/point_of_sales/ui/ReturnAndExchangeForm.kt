package com.example.capstoneproject.point_of_sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.point_of_sales.data.firebase.Product
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import com.example.capstoneproject.supplier_management.ui.RemoveProductDialog
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.supplier_management.ui.return_order.ReturnOrderViewModel
import com.example.capstoneproject.supplier_management.ui.return_order.ReturnedProductsViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReturnAndExchangeForm(
    userId: String,
    invoiceId: String,
    posViewModel: POSViewModel,
    returnOrderViewModel: ReturnOrderViewModel,
    contactViewModel: ContactViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel = viewModel(),
    back: () -> Unit
) {
    val soldProductsViewModel: SoldProductsViewModel = viewModel()
    val products = productViewModel.getAll()
    val returnedProductsViewModel: ReturnedProductsViewModel = viewModel()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var productToRemove: Product? = null
    val branchId = remember { posViewModel.getDocument(id = invoiceId)!!.branchId }
    val invoice = posViewModel.getDocument(id = invoiceId)!!
    var expanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(InvoiceType.REFUND) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("Issue Return & Exchange").uppercase()) }, navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    IconButton(
                        enabled = soldProductsViewModel.sales.isNotEmpty(),
                        onClick = { showConfirmationDialog = true }
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showProductDialog = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier
                .padding(16.dp)) {
                if (soldProductsViewModel.sales.isEmpty()) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), label = {
                            Text(text = "Type")
                        }, modifier = Modifier.fillMaxWidth(), value = type.toString(), readOnly = true, onValueChange = {  })
                        DropdownMenu(modifier = Modifier
                            .exposedDropdownSize()
                            .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(text = InvoiceType.REFUND.name) },
                                onClick = { type = InvoiceType.REFUND; expanded = false }
                            )

                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(text = InvoiceType.EXCHANGE.name) },
                                onClick = { type = InvoiceType.EXCHANGE; expanded = false }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), label = {
                        Text(text = "Type")
                    }, modifier = Modifier.fillMaxWidth(), enabled = false, value = type.toString(), readOnly = true, onValueChange = {  })
                }
            }

            androidx.compose.material3.ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Product", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(text = "Total")
                    }
                },
                trailingContent = {
                    Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                },
                tonalElevation = 5.dp
            )

            Divider()

            LazyColumn {
                itemsIndexed(soldProductsViewModel.sales) {
                        _, product ->
                    ProductItem(products = products, supplier = suppliers.value.firstOrNull { contact -> contact.id == product.supplier }?.name ?: "Unknown Supplier", product = product) {
                        productToRemove = it
                        showDeleteDialog = true
                    }
                    Divider()
                }

                item {
                    androidx.compose.material3.ListItem(
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Total Cost:", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(text = soldProductsViewModel.sales.sumOf { (it.price * it.quantity) }.toString())
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = {  }, enabled = false) { }
                        }
                    )
                }
            }

            if (showProductDialog) {
                AddProductDialogForReturnAndExchange(
                    onDismissRequest = { showProductDialog = false },
                    submit = { id, price, quantity, supplier ->
                        soldProductsViewModel.sales.add(
                            Product(
                                id = id,
                                price = price,
                                quantity = quantity,
                                supplier = supplier
                            )
                        )
                        showProductDialog = false
                    },
                    branchId = branchId,
                    products = products.filter { it.key in invoice.products.values.map { product -> product.id } }.filter { invoice.products.values.associateBy { value -> value.id }[it.key]?.let { product -> !product.returned } ?: true }.filter { it.key !in soldProductsViewModel.sales.map { products -> products.id } },
                    invoicedProducts = invoice.products,
                    type = type
                )
            }

            if (showDeleteDialog) {
                RemoveProductDialog(productName = productViewModel.getProduct(productToRemove!!.id)!!.productName, dismissRequest = { showDeleteDialog = false }) {
                    soldProductsViewModel.sales.remove(productToRemove)
                    showDeleteDialog = false
                }
            }

            if (showConfirmationDialog) {
                ConfirmationDialogForReturnAndExchange(onCancel = { showConfirmationDialog = false }) { check ->
                    posViewModel.insert(
                        Invoice(
                            date = LocalDate.now().toString(),
                            originalInvoiceId = invoiceId,
                            branchId = branchId,
                            userId = userId,
                            invoiceType = type,
                            products = soldProductsViewModel.sales.let {
                                it.forEach { sale ->
                                    productViewModel.getProduct(id = sale.id)?.let {
                                            product ->
                                        productViewModel.insert(id = sale.id, product = run {
                                            val stock = product.stock.toMutableMap()
                                            if (type == InvoiceType.REFUND) {
                                                stock[branchId] = (stock[branchId] ?: 0) + sale.quantity
                                            } else if (type == InvoiceType.EXCHANGE) {
                                                stock[branchId]?.let { quantity ->
                                                    if (quantity < sale.quantity) {
                                                        it.remove(sale)
                                                    } else {
                                                        stock[branchId] = quantity - sale.quantity
                                                    }
                                                }
                                            }

                                            product.copy(stock = stock, transaction = product.transaction.let { transaction ->
                                                if (type == InvoiceType.REFUND) {
                                                    transaction.copy(sold = transaction.sold - sale.quantity)
                                                } else { transaction }
                                            })
                                        })
                                    }
                                }

                                it.associateBy { product ->
                                    "Item ${soldProductsViewModel.sales.indexOf(product)}"
                                }
                            }
                        )
                    )

                    posViewModel.insert(
                        invoice = invoice.copy(
                            products = invoice.products.mapValues { original ->
                                val map = soldProductsViewModel.sales.map { it.id }

                                if (original.value.id in map) {
                                    original.value.copy(returned = true)
                                } else {
                                    original.value
                                }
                            }
                        )
                    )

                    userViewModel.log("${type.name.lowercase()}_invoice")

                    if (check) {
                        for (sale in soldProductsViewModel.sales) {
                            returnedProductsViewModel.returns.add(element = com.example.capstoneproject.supplier_management.data.firebase.Product(id = sale.id, quantity = sale.quantity, supplier = sale.supplier))
                        }

                        returnOrderViewModel.insert(returnOrder = ReturnOrder(
                            date = LocalDate.now().toString(),
                            status = Status.WAITING,
                            reason = "Returned by customer",
                            branchId = branchId,
                            products = returnedProductsViewModel.returns.associateBy { product -> "Item ${returnedProductsViewModel.returns.indexOf(product)}" }
                        ))

                        userViewModel.log("create_return_order")
                    }

                    back.invoke()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddProductDialogForReturnAndExchange(
    onDismissRequest: () -> Unit,
    submit: (String, Double, Int, String) -> Unit,
    type: InvoiceType,
    branchId: String,
    products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>,
    invoicedProducts: Map<String, Product>
) {
    var search = products
    var expanded by remember { mutableStateOf(false) }
    var isQuantityValid by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("") }
    var quantity = 0
    var maxQuantity = 0
    var selectedProduct by remember { mutableStateOf("") }
    var price = 0.0
    var productId = ""
    var supplier = ""
    var canSubmit by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(enabled = selectedProduct.isNotBlank(), onClick = {
                quantityText.toIntOrNull()?.let { if (it > 0) { quantity = it; isQuantityValid = true } else isQuantityValid = false } ?: run { isQuantityValid = false }
                if (isQuantityValid) {
                    submit.invoke(productId, price, quantity, supplier)
                }
            }) {
                Text(text = "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent)) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(text = "Add Product")
        },
        icon = {
            Icon(imageVector = Icons.Outlined.AddCircleOutline, contentDescription = null)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = GlobalTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        value = selectedProduct,
                        isError = !canSubmit,
                        onValueChange = {
                            selectedProduct = it
                            canSubmit = false
                            search = products.filter { map ->
                                map.value.productName.contains(other = selectedProduct, ignoreCase = true)
                            }
                        },
                        label = { Text(text = stringResource(id = R.string.product)) }
                    )

                    DropdownMenu(
                        modifier = Modifier
                            .exposedDropdownSize()
                            .requiredHeightIn(max = 300.dp)
                            .fillMaxWidth(),
                        expanded = expanded,
                        onDismissRequest = {  },
                        properties = PopupProperties(focusable = false)
                    ) {
                        search.forEach {
                            val itemQuantity = invoicedProducts.filterValues { product -> product.id == it.key }.values.first().quantity
                            val quantityInStock = it.value.stock.getOrDefault(key = branchId, defaultValue = 0)
                            androidx.compose.material3.DropdownMenuItem(text = {
                                Column {
                                    Text(text = it.value.productName)
                                    Text(text = "$itemQuantity Units in invoice", color = Color(red = 0f, green = 0.8f, blue = 0f))
                                    quantityInStock.let { count ->
                                        if (count > 0) {
                                            Text(text = "$count Units currently in Stock", color = Color(red = 0f, green = 0.8f, blue = 0f))
                                        } else {
                                            Text(text = "Out of Stock", color = MaterialTheme.colors.error)
                                        }
                                    }
                                }
                            }, onClick = {
                                canSubmit = true
                                productId = it.key
                                selectedProduct = it.value.productName
                                price = it.value.sellingPrice
                                maxQuantity = if (type == InvoiceType.REFUND) itemQuantity else if (itemQuantity > quantityInStock) quantityInStock else itemQuantity
                                supplier = it.value.supplier
                                expanded = false
                            })
                        }
                    }
                }

                androidx.compose.material3.OutlinedTextField(
                    trailingIcon = {
                        if (!isQuantityValid) Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    },
                    supportingText = {
                        if (!isQuantityValid) Text(
                            text = "Enter valid quantity only!",
                            color = Color.Red
                        )
                    },
                    isError = !isQuantityValid,
                    value = quantityText,
                    onValueChange = {
                        it.toIntOrNull()?.let { input ->
                            if (maxQuantity > input) {
                                quantityText = if (input > 0) it else ""; isQuantityValid = true
                            } else quantityText = maxQuantity.toString()
                        } ?: run {
                            if (it.isNotBlank()) isQuantityValid = false else quantityText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Quantity") },
                    placeholder = { Text(text = "Enter Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}

@Composable
fun ConfirmationDialogForReturnAndExchange(
    onCancel: () -> Unit,
    onSubmit: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(true) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Proceed with submission?")
        },
        text = {
            Column {
                Text(text = "Are you sure you want to make this transaction?")
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                    Text(text = "Create a return order based on this document")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit.invoke(checked) }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Send, contentDescription = null)
        }
    )
}
