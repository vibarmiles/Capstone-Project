package com.example.capstoneproject.supplier_management.ui.return_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmationDialog
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.Product
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import com.example.capstoneproject.supplier_management.ui.AddProductDialog
import com.example.capstoneproject.supplier_management.ui.ProductItem
import com.example.capstoneproject.supplier_management.ui.RemoveProductDialog
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReturnOrderForm(
    contactViewModel: ContactViewModel,
    returnOrderViewModel: ReturnOrderViewModel,
    branchViewModel: BranchViewModel,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    returnOrderId: String? = null,
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
    val showConfirmationDialog = remember { mutableStateOf(false) }
    val localFocusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { androidx.compose.material.Text(text = ("Add " + stringResource(R.string.return_order)).uppercase()) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(enabled = branches.value.isNotEmpty() && returnedProductsViewModel.returns.isNotEmpty(),
                        onClick = {
                            showConfirmationDialog.value = true
                        }
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
        rememberSaveable {
            returnOrderId.let {
                if (it != null) {
                    returnedProductsViewModel.returns.addAll(returnOrderViewModel.getDocument(it)?.products?.values ?: listOf())
                } else ""
            }
        }

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
                            OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), label = { Text(text = "Return Item from this branch") }, modifier = Modifier.fillMaxWidth(), value = textFieldValue, readOnly = true, onValueChange = {  })
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

                    OutlinedTextField(
                        value = reason,
                        colors = GlobalTextFieldColors(),
                        label = { Text(text = "Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = { reason = it },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            localFocusManager.clearFocus()
                        })
                    )
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
                        Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
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
                    returnedProductsViewModel.returns.find { it.id == id }.let { product ->
                        if (product == null) {
                            returnedProductsViewModel.returns.add(
                                Product(
                                    id = id,
                                    quantity = quantity,
                                    supplier = supplier
                                )
                            )
                        } else {
                            returnedProductsViewModel.returns.set(returnedProductsViewModel.returns.indexOf(product), product.copy(quantity = product.quantity + quantity))
                        }
                    }
                    showProductDialog = false
                },
                branchId = branchId,
                products = products.mapValues { product -> product.value.copy(stock = product.value.stock.mapValues { stock -> stock.value - (returnedProductsViewModel.returns.find { it.id == product.key }?.quantity ?: 0) }) },
                suppliers = suppliers.value
            )
        }

        if (showDeleteDialog) {
            RemoveProductDialog(productName = productViewModel.getProduct(productToRemove!!.id)!!.productName, dismissRequest = { showDeleteDialog = false }) {
                returnedProductsViewModel.returns.remove(productToRemove)
                showDeleteDialog = false
            }
        }

        if (showConfirmationDialog.value) {
            ConfirmationDialog(onCancel = { showConfirmationDialog.value = false }) {
                returnOrderViewModel.insert(
                    ReturnOrder(
                        id = returnOrderId ?: "",
                        status = Status.WAITING,
                        reason = reason,
                        branchId = branchId!!,
                        products = returnedProductsViewModel.returns.associateBy { product -> "Item ${returnedProductsViewModel.returns.indexOf(product)}" }
                    )
                )
                showConfirmationDialog.value = false
                userViewModel.log(event = "create_return_order")
                back.invoke()
            }
        }
    }
}