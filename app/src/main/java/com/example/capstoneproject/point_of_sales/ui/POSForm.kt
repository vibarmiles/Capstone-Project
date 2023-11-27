package com.example.capstoneproject.point_of_sales.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.point_of_sales.data.firebase.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.supplier_management.ui.RemoveProductDialog
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun POSForm(
    userId: String,
    posViewModel: POSViewModel,
    contactViewModel: ContactViewModel,
    branchViewModel: BranchViewModel,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    back: () -> Unit
) {
    val soldProductsViewModel: SoldProductsViewModel = viewModel()
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val branches = branchViewModel.getAll().observeAsState(listOf())
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val state = posViewModel.result.collectAsState()
    val context = LocalContext.current
    var productToRemove: Product? = null
    var branchId by remember { mutableStateOf(branches.value.firstOrNull()?.id) }
    var textFieldValue by remember { mutableStateOf(branches.value.firstOrNull()?.name ?: "No Branches Found") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("Add Sales Invoice").uppercase()) }, navigationIcon = {
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (soldProductsViewModel.sales.isEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded
                                    )
                                },
                                colors = GlobalTextFieldColors(),
                                label = {
                                    androidx.compose.material3.Text(text = "Sell Item from this branch")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                value = textFieldValue,
                                readOnly = true,
                                onValueChange = { })
                            DropdownMenu(modifier = Modifier
                                .exposedDropdownSize()
                                .fillMaxWidth(),
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                branches.value.forEach { branch ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(text = branch.name) },
                                        onClick = {
                                            branchId = branch.id
                                            textFieldValue = branch.name
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                androidx.compose.material3.Text(
                                    text = "Sell Item from this branch"
                                )
                            },
                            value = textFieldValue,
                            enabled = false,
                            readOnly = true,
                            onValueChange = { })
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
            }
        }

        if (showProductDialog) {
            AddProductDialog(
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
                products = products.filter { it.key !in soldProductsViewModel.sales.map { products -> products.id } },
                branchId = branchId!!
            )
        }

        if (showDeleteDialog) {
            RemoveProductDialog(productName = productViewModel.getProduct(productToRemove!!.id)!!.productName, dismissRequest = { showDeleteDialog = false }) {
                soldProductsViewModel.sales.remove(productToRemove)
                showDeleteDialog = false
            }
        }

        if (showConfirmationDialog) {
            ConfirmationDialog(onCancel = { showConfirmationDialog = false }) {
                posViewModel.transact(
                    Invoice(
                        branchId = branchId!!,
                        userId = userId,
                        products = soldProductsViewModel.sales.let {
                            it.associateBy { product ->
                                "Item ${soldProductsViewModel.sales.indexOf(product)}"
                            }
                        }
                    )
                )
            }
        }

        LaunchedEffect(key1 = state.value) {
            if (!state.value.result && state.value.errorMessage != null) {
                Toast.makeText(context, state.value.errorMessage!!, Toast.LENGTH_SHORT).show()
                posViewModel.resetMessage()
            } else if (state.value.result) {
                userViewModel.log("create_invoice")
                back.invoke()
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
            Text(text = (products[product.id])!!.productName, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Supplier: $supplier", maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Price: ${ product.price }", modifier = Modifier.weight(1f))
                    Text(text = (product.price * product.quantity).toString())
                }
                Text(text = "Qty: ${ product.quantity }")
            }
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
    branchId: String,
    submit: (String, Double, Int, String) -> Unit,
    products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>
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
                            androidx.compose.material3.DropdownMenuItem(text = {
                                Column {
                                    Text(text = it.value.productName)
                                    it.value.stock.getOrDefault(key = branchId, defaultValue = 0).let { count ->
                                        if (count > 0) {
                                            Text(text = "$count Units", color = Color(red = 0f, green = 0.8f, blue = 0f))
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
                                maxQuantity = it.value.stock.getOrDefault(key = branchId, defaultValue = 0)
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
                    label = {
                        Text(text = "Quantity")
                    },
                    placeholder = { Text(text = "Enter Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}

@Composable
fun ConfirmationDialog(
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Proceed with submission?")
        },
        text = {
            Text(text = "Are you sure you want to make this transaction?")
        },
        confirmButton = {
            Button(onClick = onSubmit) {
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
