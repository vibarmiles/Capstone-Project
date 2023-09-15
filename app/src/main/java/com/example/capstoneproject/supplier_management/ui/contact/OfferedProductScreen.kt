package com.example.capstoneproject.supplier_management.ui.contact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.product.ProductViewModel

@Composable
fun OfferedProductScreen(contactViewModel: ContactViewModel, productViewModel: ProductViewModel, contactId: String, contactName: String, product: Map<String, Double>, back: () -> Unit) {
    val offerViewModel: OfferedProductViewModel = viewModel()
    val products = productViewModel.products.toMap()
    var showSaveDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pair: Pair<String, String>? = null
    product.forEach { (key, value) -> offerViewModel.offers[key] = value }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Offered Products by $contactName") }, navigationIcon = {
                IconButton(onClick = { showSaveDialog = true }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            }, actions = {
                IconButton(onClick = { contactViewModel.addProductForSupplier(key = contactId, product = offerViewModel.offers.filterKeys { it in products.keys }); back.invoke() }) {
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
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

            if (products.isEmpty()) {
                item {
                    Text(modifier = Modifier.padding(16.dp), text = "This supplier currently has no offered products entered")
                }
            }

            itemsIndexed(products.filterKeys { it in offerViewModel.offers.keys }.toList()) {
                _, it ->
                androidx.compose.material3.ListItem(
                    leadingContent = { AsyncImage(error = rememberVectorPainter(image = Icons.Filled.Image), fallback = rememberVectorPainter(image = Icons.Filled.Image), model = it.second.image ?: "", contentScale = ContentScale.Crop, modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .size(50.dp), placeholder = rememberVectorPainter(Icons.Default.Image), contentDescription = null) },
                    headlineContent = { offerViewModel.offers[it.first] },
                    supportingContent = { Text(text = it.second.productName) },
                    trailingContent = { IconButton(onClick = { pair = Pair(it.first, it.second.productName); showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                    }
                })
            }
        }

        if (showSaveDialog) {
            SaveDialog(onDismissRequest = { showSaveDialog = false }, cancel = { back.invoke() }) {
                contactViewModel.addProductForSupplier(key = contactId, product = offerViewModel.offers.filterKeys { it in products.keys })
                back.invoke()
            }
        }

        if (showProductDialog) {
            AddProductDialog(onDismissRequest = { showProductDialog = false }, products = products.filterKeys { it !in offerViewModel.offers.keys }, submit = { id, price -> offerViewModel.offers[id] = price; showProductDialog = false })
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = pair!!.second, onCancel = { showDeleteDialog = false }) {
                offerViewModel.offers.remove(pair!!.first)
                showDeleteDialog = false
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddProductDialog(onDismissRequest: () -> Unit, submit: (String, Double) -> Unit, products: Map<String, Product>) {
    var expanded by remember { mutableStateOf(false) }
    var isPriceValid by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }
    var id = ""
    var price = 0.0
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = {
                text.toDoubleOrNull()?.let { price = it; isPriceValid = true } ?: run { isPriceValid = false }
                if (isPriceValid && id.isNotBlank()) {
                    submit.invoke(id, price)
                }
            }) {
                Text(text = "Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(text = "Add Product")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    var selectedProduct by remember { mutableStateOf("") }
                    OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = selectedProduct, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.product)) })

                    DropdownMenu(modifier = Modifier
                        .exposedDropdownSize()
                        .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {

                        products.forEach {
                            androidx.compose.material3.DropdownMenuItem(text = { Text(text = it.value.productName) }, onClick = { id = it.key; selectedProduct = it.value.productName; expanded = false })
                        }
                    }
                }

                androidx.compose.material3.OutlinedTextField(trailingIcon = { if (!isPriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, supportingText = { if (!isPriceValid) Text(text = "Enter valid prices only!", color = Color.Red) }, isError = !isPriceValid, value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), label = { Text(text = "Price") }, placeholder = { Text(text = "Enter Supplier's Offered Price for the item") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    )
}

@Composable
fun SaveDialog(onDismissRequest: () -> Unit, cancel: () -> Unit, onSubmit: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onSubmit) {
                Text(text = "SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = cancel) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        text = {
            Text(text = "You have unsaved changes that will be lost if you decide to continue. \n\nSave changes before closing?")
        },
        title = {
            Text(text = "Save Changes?")
        },
        icon = {
            Icon(contentDescription = null, imageVector = Icons.Filled.Save)
        }
    )
}