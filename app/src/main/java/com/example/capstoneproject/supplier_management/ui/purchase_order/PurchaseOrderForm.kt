package com.example.capstoneproject.supplier_management.ui.purchase_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Product
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Status
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PurchaseOrderForm(contactViewModel: ContactViewModel, purchaseOrderViewModel: PurchaseOrderViewModel, productViewModel: ProductViewModel, back: () -> Unit) {
    val purchasedProductsViewModel: PurchasedProductsViewModel = viewModel()
    var expanded by remember { mutableStateOf(false) }
    val contacts = contactViewModel.contacts
    val products = productViewModel.products
    var textFieldValue by remember { mutableStateOf(contacts.values.first().name) }
    var supplierId: String by remember { mutableStateOf(contacts.keys.first()) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToRemove: Product? = null

    Scaffold(
        topBar = {
            TopAppBar(title = { androidx.compose.material.Text(text = "Add " + stringResource(R.string.purchase_order)) }, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            }, actions = {
                IconButton(onClick = {
                    purchaseOrderViewModel.insert(PurchaseOrder(supplier = supplierId, date = LocalDate.now().toString(), status = Status.WAITING, products = purchasedProductsViewModel.purchases.associateBy { product -> "Item ${purchasedProductsViewModel.purchases.indexOf(product)}" }))
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
    ) {
        paddingValues -> 
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = textFieldValue, readOnly = true, onValueChange = {  })
                DropdownMenu(modifier = Modifier
                    .exposedDropdownSize()
                    .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                    contacts.forEach {
                            s ->
                        DropdownMenuItem(text = { androidx.compose.material.Text(text = s.value.name) }, onClick = { textFieldValue = s.value.name; supplierId = s.key; expanded = false })
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Product", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text(text = "Total")
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = {  }, enabled = false) {
                            Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                        }
                    }
                )

                Divider()

                LazyColumn {
                    itemsIndexed(purchasedProductsViewModel.purchases) {
                            _, product ->
                        ProductItem(product = product) {
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
            AddProductDialog(offers = contacts[supplierId]?.product ?: mapOf(), onDismissRequest = { showProductDialog = false }, submit = {
                name, price, quantity -> purchasedProductsViewModel.purchases.add(Product(name = name, price = price, quantity = quantity)); showProductDialog = false
            }, products = products.filter { it.value.productName !in purchasedProductsViewModel.purchases.map { products -> products.name } })
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = productToRemove!!.name, onCancel = { showDeleteDialog = false }) {
                purchasedProductsViewModel.purchases.remove(productToRemove)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, remove: (Product) -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = {
            Text(text = product.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
fun AddProductDialog(offers: Map<String, Double>, onDismissRequest: () -> Unit, submit: (String, Double, Int) -> Unit, products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>) {
    var expanded by remember { mutableStateOf(false) }
    var isPriceValid by remember { mutableStateOf(true) }
    var priceText by remember { mutableStateOf("") }
    var price = 0.0
    var isQuantityValid by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("") }
    var quantity = 0
    var selectedProduct by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = {
                priceText.toDoubleOrNull()?.let { price = it; isPriceValid = true } ?: run { isPriceValid = false }
                quantityText.toIntOrNull()?.let { quantity = it; isQuantityValid = true } ?: run { isQuantityValid = false }
                if (isPriceValid && isQuantityValid && selectedProduct.isNotBlank()) {
                    submit.invoke(selectedProduct, price, quantity)
                }
            }) {
                androidx.compose.material.Text(text = "Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                androidx.compose.material.Text(text = "Cancel")
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
                                androidx.compose.material.Text(
                                    text = it.value.productName
                                )
                            }, onClick = {
                                selectedProduct = it.value.productName
                                if (offers.containsKey(it.key)) {
                                    priceText = offers[it.key].toString()
                                }
                                expanded = false
                            })
                        }
                    }
                }

                androidx.compose.material3.OutlinedTextField(trailingIcon = { if (!isPriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, supportingText = { if (!isPriceValid) androidx.compose.material.Text(
                    text = "Enter valid prices only!",
                    color = Color.Red
                ) }, isError = !isPriceValid, value = priceText, onValueChange = { priceText = it }, modifier = Modifier.fillMaxWidth(), label = {
                    androidx.compose.material.Text(text = "Price")
                }, placeholder = { androidx.compose.material.Text(text = "Enter Supplier's Offered Price for the item") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                androidx.compose.material3.OutlinedTextField(trailingIcon = { if (!isQuantityValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, supportingText = { if (!isQuantityValid) androidx.compose.material.Text(
                    text = "Enter valid quantity only!",
                    color = Color.Red
                ) }, isError = !isQuantityValid, value = quantityText, onValueChange = { quantityText = it }, modifier = Modifier.fillMaxWidth(), label = {
                    androidx.compose.material.Text(text = "Quantity")
                }, placeholder = { androidx.compose.material.Text(text = "Enter Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    )
}