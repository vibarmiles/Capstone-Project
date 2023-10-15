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
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Product
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Status
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import java.time.LocalDate

@Composable
fun PurchaseOrderForm(contactViewModel: ContactViewModel, purchaseOrderViewModel: PurchaseOrderViewModel, productViewModel: ProductViewModel, back: () -> Unit) {
    val purchasedProductsViewModel: PurchasedProductsViewModel = viewModel()
    val contacts = contactViewModel.getAll().observeAsState()
    val products = productViewModel.getAll()
    var supplierId: String by remember { mutableStateOf(contacts.value?.first()?.id ?: "") }
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
                .padding(paddingValues),
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
                        IconButton(onClick = {  }, enabled = false) {
                            Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                        }
                    },
                    tonalElevation = 5.dp
                )

                Divider()

                LazyColumn {
                    itemsIndexed(purchasedProductsViewModel.purchases) {
                            _, product ->
                        ProductItem(products = products, product = product) {
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
            AddProductDialog(onDismissRequest = { showProductDialog = false }, submit = {
                id, price, quantity -> purchasedProductsViewModel.purchases.add(Product(id = id, price = price, quantity = quantity)); showProductDialog = false
            }, products = products.filter { it.key !in purchasedProductsViewModel.purchases.map { products -> products.id } })
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = productToRemove!!.id, onCancel = { showDeleteDialog = false }) {
                purchasedProductsViewModel.purchases.remove(productToRemove)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun ProductItem(products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>, product: Product, remove: (Product) -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = {
            Text(text = (products[product.id])!!.productName, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
fun AddProductDialog(onDismissRequest: () -> Unit, submit: (String, Double, Int) -> Unit, products: Map<String, com.example.capstoneproject.product_management.data.firebase.product.Product>) {
    var expanded by remember { mutableStateOf(false) }
    var isQuantityValid by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("") }
    var quantity = 0
    var selectedProduct by remember { mutableStateOf("") }
    var price = 0.0
    var productId = ""
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(enabled = selectedProduct.isNotBlank(), onClick = {
                quantityText.toIntOrNull()?.let { quantity = it; isQuantityValid = true } ?: run { isQuantityValid = false }
                if (isQuantityValid) {
                    submit.invoke(productId, price, quantity)
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
                                price = it.value.purchasePrice
                                expanded = false
                            })
                        }
                    }
                }

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