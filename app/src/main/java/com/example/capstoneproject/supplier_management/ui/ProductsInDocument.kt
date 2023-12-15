package com.example.capstoneproject.supplier_management.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.supplier_management.data.firebase.Product

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
                Text(text = (products[product.id])?.productName ?: "Unknown Product", maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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
    var search = products
    var expanded by remember { mutableStateOf(false) }
    var isQuantityValid by remember { mutableStateOf(true) }
    var quantityText by remember { mutableStateOf("") }
    var quantity = 0
    var maxQuantity = 0
    var selectedProduct by remember { mutableStateOf("") }
    var productId = ""
    var supplier = ""
    var canSubmit by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(enabled = selectedProduct.isNotBlank(), onClick = {
                quantityText.toIntOrNull()?.let { if (it > 0) { quantity = it; isQuantityValid = true } } ?: run { isQuantityValid = false }
                if (isQuantityValid) {
                    submit.invoke(productId, quantity, supplier)
                }
            }) {
                androidx.compose.material.Text(text = "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent)) {
                androidx.compose.material.Text(text = "Cancel")
            }
        },
        title = {
            androidx.compose.material.Text(text = "Add Product")
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
                            productId = ""
                            canSubmit = false
                            selectedProduct = it
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
                                    it.value.stock.getOrDefault(key = branchId, defaultValue = 0).let { count ->
                                        if (count == 0) {
                                            Text(text = "Out of Stock", color = MaterialTheme.colors.error)
                                        } else {
                                            Text(text = "$count Units", color = Color(red = 0f, green = 0.8f, blue = 0f))
                                        }
                                    }
                                }
                            }, onClick = {
                                canSubmit = true
                                productId = it.key
                                selectedProduct = it.value.productName
                                quantityText = ""
                                maxQuantity = it.value.stock.getOrDefault(key = branchId, defaultValue = 0)
                                supplier = it.value.supplier
                                expanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    trailingIcon = { if (!isQuantityValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                    supportingText = { if (!isQuantityValid) androidx.compose.material.Text(
                        text = "Enter valid quantity only!",
                        color = Color.Red
                        )
                    },
                    isError = !isQuantityValid, value = quantityText,
                    onValueChange = { it.toIntOrNull()?.let { input -> quantityText = if (maxQuantity > input) { if (input > 0) it else "" } else maxQuantity.toString() } ?: run { if (it.isBlank()) quantityText = "" } },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        androidx.compose.material.Text(text = "Quantity")
                    },
                    placeholder = { androidx.compose.material.Text(text = "Enter Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        icon = {
            Icon(imageVector = Icons.Outlined.AddCircleOutline, contentDescription = null)
        }
    )
}

@Composable
fun RemoveProductDialog(
    productName: String,
    dismissRequest: () -> Unit,
    submitRequest: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = dismissRequest,
        confirmButton = {
            Button(onClick = submitRequest) {
                androidx.compose.material.Text(text = "Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = dismissRequest, colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent)) {
                androidx.compose.material.Text(text = "Cancel")
            }
        },
        title = {
            androidx.compose.material.Text(text = "Confirm Removal")
        },
        text = {
            androidx.compose.material.Text("Are you sure you want to remove $productName from document's list of products?")
        },
        icon = {
            Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
        }
    )
}