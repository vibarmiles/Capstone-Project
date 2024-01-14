package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.capstoneproject.global.ui.misc.ConfirmationDialog
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Product
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.ui.RemoveProductDialog
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun PurchaseOrderForm(
    contactViewModel: ContactViewModel,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    purchaseOrderId: String? = null,
    productId: String? = null,
    back: () -> Unit
) {
    val purchasedProductsViewModel: PurchasedProductsViewModel = viewModel()
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    var showInitialProductDialog by remember { mutableStateOf(productId != null) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val showConfirmationDialog = remember { mutableStateOf(false) }
    var productToRemove: Product? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("Add " + stringResource(R.string.purchase_order)).uppercase()) }, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            }, actions = {
                IconButton(
                    enabled = purchasedProductsViewModel.purchases.isNotEmpty(),
                    onClick = {
                        showConfirmationDialog.value = true
                    }
                ) {
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
        rememberSaveable {
            purchaseOrderId.let {
                if (it != null) {
                    purchasedProductsViewModel.purchases.addAll(purchaseOrderViewModel.getDocument(it)?.products?.values ?: listOf())

                } else ""
            }
        }

        Column(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    itemsIndexed(purchasedProductsViewModel.purchases) { _, product ->
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
                                    Text(text = purchasedProductsViewModel.purchases.sumOf { (it.price * it.quantity) }.toString())
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
                    purchasedProductsViewModel.purchases.add(Product(id = id, price = price, quantity = quantity, supplier = supplier))
                    showProductDialog = false
                },
                products = products.filter { it.key !in purchasedProductsViewModel.purchases.map { products -> products.id } },
                productViewModel = productViewModel, date = Instant.ofEpochMilli(userViewModel.userAccountDetails.collectAsState().value.loginDate).atZone(ZoneId.systemDefault()).toLocalDate(),
                suppliers = suppliers.value
            )
        }

        if (showInitialProductDialog) {
            AddProductDialog(
                onDismissRequest = { showInitialProductDialog = false },
                submit = { id, price, quantity, supplier ->
                    purchasedProductsViewModel.purchases.add(Product(id = id, price = price, quantity = quantity, supplier = supplier))
                    showInitialProductDialog = false
                },
                products = products.filter { it.key !in purchasedProductsViewModel.purchases.map { products -> products.id } },
                productViewModel = productViewModel, date = Instant.ofEpochMilli(userViewModel.userAccountDetails.collectAsState().value.loginDate).atZone(ZoneId.systemDefault()).toLocalDate(),
                initial = productId,
                suppliers = suppliers.value
            )
        }

        if (showDeleteDialog) {
            RemoveProductDialog(productName = productViewModel.getProduct(productToRemove?.id)?.productName ?: "Unknown Product", dismissRequest = { showDeleteDialog = false }) {
                purchasedProductsViewModel.purchases.remove(productToRemove)
                showDeleteDialog = false
            }
        }

        if (showConfirmationDialog.value) {
            ConfirmationDialog(onCancel = { showConfirmationDialog.value = false }) {
                purchaseOrderViewModel.insert(
                    PurchaseOrder(
                        id = purchaseOrderId ?: "",
                        status = Status.WAITING,
                        products = purchasedProductsViewModel.purchases.associateBy { product ->
                            "Item ${purchasedProductsViewModel.purchases.indexOf(product)}"
                        }
                    )
                )
                showConfirmationDialog.value = false
                userViewModel.log(event = "create_purchase_order")
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
            Text(text = (products[product.id])?.productName ?: "Unknown Product", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    initial: String? = null,
    submit: (String, Double, Int, String) -> Unit,
    date: LocalDate,
    products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>,
    productViewModel: ProductViewModel,
    suppliers: List<Contact>
) {
    var search = products
    var expanded by remember { mutableStateOf(false) }
    var isQuantityValid by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("") }
    var quantity = 0
    var selectedProduct by remember { mutableStateOf(if (initial != null) products[initial]?.productName ?: "" else "") }
    var price = 0.0
    var productId = initial ?: ""
    var supplier = ""
    var canSubmit by remember { mutableStateOf(false) }
    val supplierValue = remember(supplier) { mutableStateOf(suppliers.firstOrNull { it.id == supplier }?.name ?: "") }
    var supplierExpanded by remember { mutableStateOf(false) }

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
                            DropdownMenuItem(text = {
                                Column {
                                    Text(text = it.value.productName)
                                    it.value.stock.count { stock -> stock.value < productViewModel.getCriticalLevel(product = it.value, date = date) }.let { count ->
                                        Text(text = when (count) {
                                            0 -> return@let
                                            1 -> "Stock is critical in 1 branch"
                                            else -> "Stock is critical in $count branches"
                                        }, color = Color.Red)
                                    }
                                }
                            }, onClick = {
                                canSubmit = true
                                productId = it.key
                                selectedProduct = it.value.productName
                                price = it.value.purchasePrice
                                supplier = it.value.supplier
                                expanded = false
                            })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = supplierExpanded, onExpandedChange = { supplierExpanded = !supplierExpanded }) {
                    OutlinedTextField(
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                        colors = GlobalTextFieldColors(),
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        value = supplierValue.value,
                        onValueChange = {  },
                        label = { Text(text = stringResource(id = R.string.supplier)) }
                    )

                    DropdownMenu(
                        modifier = Modifier
                            .exposedDropdownSize()
                            .requiredHeightIn(max = 300.dp)
                            .fillMaxWidth(),
                        expanded = supplierExpanded,
                        onDismissRequest = { supplierExpanded = false },
                        properties = PopupProperties(focusable = false)
                    ) {
                        suppliers.filter {
                            it.id in products.filter { product -> product.value.productName == selectedProduct }.map { product -> product.value.supplier }
                        }.forEach {
                            DropdownMenuItem(text = {
                                Column {
                                    Text(text = it.name)
                                }
                            }, onClick = {
                                supplier = it.id
                                supplierValue.value = it.name
                                productId = products.toList().first { product ->
                                    product.second.productName == selectedProduct && product.second.supplier == it.id
                                }.first
                                price = products[productId]?.purchasePrice ?: 0.0
                                supplierExpanded = false
                            })
                        }
                    }
                }

                androidx.compose.material3.OutlinedTextField(trailingIcon = { if (!isQuantityValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, supportingText = { if (!isQuantityValid) Text(
                    text = "Enter valid quantity only!",
                    color = Color.Red
                ) }, isError = !isQuantityValid, value = quantityText, onValueChange = { it.toIntOrNull()?.let { input -> quantityText = if (input < 0) "" else input.toString() } ?: run { if (it.isBlank()) quantityText = "" } }, modifier = Modifier.fillMaxWidth(), label = {
                    Text(text = "Quantity")
                }, placeholder = { Text(text = "Enter Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    )
}