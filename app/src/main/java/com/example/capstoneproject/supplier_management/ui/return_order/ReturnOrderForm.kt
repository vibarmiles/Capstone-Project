package com.example.capstoneproject.supplier_management.ui.return_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.return_order.Product
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReturnOrderForm(
    contactViewModel: ContactViewModel,
    returnOrderViewModel: ReturnOrderViewModel,
    branchViewModel: BranchViewModel,
    productViewModel: ProductViewModel,
    back: () -> Unit
) {
    val returnedProductsViewModel: ReturnedProductsViewModel = viewModel()
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val branches = branchViewModel.getAll().observeAsState(listOf())
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToRemove: Product? = null
    var branchId by remember { mutableStateOf(branches.value.firstOrNull()?.id) }
    var textFieldValue by remember { mutableStateOf(branches.value.firstOrNull()?.name ?: "No Branches Found") }
    var reason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { androidx.compose.material.Text(text = "Add " + stringResource(R.string.return_order)) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(enabled = branches.value.isNotEmpty(),
                        onClick = {
                        returnOrderViewModel.insert(
                            ReturnOrder(
                                date = LocalDate.now().toString(),
                                status = Status.WAITING,
                                reason = reason,
                                branchId = branchId!!,
                                products = returnedProductsViewModel.returns.associateBy { product ->
                                    "Item ${
                                        returnedProductsViewModel.returns.indexOf(product)
                                    }"
                                })
                        )
                        back.invoke()
                    }) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showProductDialog = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (returnedProductsViewModel.returns.isEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, label = { Text(text = "Return Item from this branch") }, modifier = Modifier.fillMaxWidth(), value = textFieldValue, readOnly = true, onValueChange = {  })
                            DropdownMenu(modifier = Modifier
                                .exposedDropdownSize()
                                .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                                branches.value.forEach {
                                        branch ->
                                    DropdownMenuItem(text = { androidx.compose.material.Text(text = branch.name) }, onClick = { branchId = branch.id; textFieldValue = branch.name; expanded = false })
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(modifier = Modifier.fillMaxWidth(), label = { Text(text = "Return Item from this branch") }, value = textFieldValue, enabled = false, readOnly = true, onValueChange = {  })
                    }

                    OutlinedTextField(value = reason, label = { Text(text = "Reason") }, modifier = Modifier.fillMaxWidth(), onValueChange = { reason = it })
                }
                
                androidx.compose.material3.ListItem(
                    headlineContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Product",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "Quantity")
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { }, enabled = false) {
                            Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                        }
                    },
                    tonalElevation = 5.dp
                )

                Divider()

                LazyColumn {
                    itemsIndexed(returnedProductsViewModel.returns) { _, product ->
                        ProductItem(
                            products = products,
                            supplier = suppliers.value.firstOrNull { contact -> contact.id == product.supplier }?.name ?: "Unknown Supplier",
                            product = product
                        ) {
                            productToRemove = it
                            showDeleteDialog = true
                        }
                    }
                }
            }
        }

        if (showProductDialog) {
            AddProductDialog(
                onDismissRequest = { showProductDialog = false },
                submit = { id, quantity, supplier ->
                    returnedProductsViewModel.returns.add(
                        Product(
                            id = id,
                            quantity = quantity,
                            supplier = supplier
                        )
                    ); showProductDialog = false
                },
                branchId = branchId,
                products = products.filter { it.key !in returnedProductsViewModel.returns.map { products -> products.id } })
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = productToRemove!!.id, onCancel = { showDeleteDialog = false }) {
                returnedProductsViewModel.returns.remove(productToRemove)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun ProductItem(
    products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>,
    supplier: String,
    product: Product,
    remove: (Product) -> Unit
) {
    androidx.compose.material3.ListItem(
        headlineContent = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = (products[product.id])!!.productName, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(text = "${ product.quantity }")
            }
        },
        supportingContent = {
            Text(text = "Supplier: $supplier", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            IconButton(onClick = { remove.invoke(product) }) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddProductDialog(
    onDismissRequest: () -> Unit,
    submit: (String, Int, String) -> Unit,
    branchId: String?,
    products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>
) {
    var expanded by remember { mutableStateOf(false) }
    var isQuantityValid by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("") }
    var quantity = 0
    var maxQuantity = 0
    var selectedProduct by remember { mutableStateOf("") }
    var productId = ""
    var supplier = ""

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(enabled = selectedProduct.isNotBlank(), onClick = {
                quantityText.toIntOrNull()?.let { quantity = it; isQuantityValid = true } ?: run { isQuantityValid = false }
                if (isQuantityValid) {
                    submit.invoke(productId, quantity, supplier)
                }
            }) {
                androidx.compose.material.Text(text = "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent)) {
                Text(text = "Cancel")
            }
        },
        title = {
            androidx.compose.material.Text(text = "Add Product")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = selectedProduct, onValueChange = {  }, readOnly = true, label = {
                        androidx.compose.material.Text(text = stringResource(id = R.string.product))
                    })

                    DropdownMenu(modifier = Modifier
                        .exposedDropdownSize()
                        .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {

                        products.forEach {
                            DropdownMenuItem(text = {
                                Text(text = it.value.productName)
                            }, onClick = {
                                productId = it.key
                                selectedProduct = it.value.productName
                                maxQuantity = it.value.stock.getOrDefault(key = branchId, defaultValue = 0)
                                supplier = it.value.supplier
                                expanded = false
                            })
                        }
                    }
                }

                androidx.compose.material3.OutlinedTextField(trailingIcon = { if (!isQuantityValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, supportingText = { if (!isQuantityValid) androidx.compose.material.Text(
                    text = "Enter valid quantity only!",
                    color = Color.Red
                ) }, isError = !isQuantityValid, value = quantityText, onValueChange = { it.toIntOrNull()?.let { input -> if (maxQuantity > input) { quantityText = if (input > 0) it else "0"; isQuantityValid = true } else quantityText = maxQuantity.toString() } ?: run { if (it.isNotBlank()) isQuantityValid = false else quantityText = "" } }, modifier = Modifier.fillMaxWidth(), label = {
                    androidx.compose.material.Text(text = "Quantity")
                }, placeholder = { androidx.compose.material.Text(text = "Enter Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    )
}