package com.example.capstoneproject.supplier_management.ui.transfer_order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Product
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrder
import com.example.capstoneproject.supplier_management.ui.AddProductDialog
import com.example.capstoneproject.supplier_management.ui.ProductItem
import com.example.capstoneproject.supplier_management.ui.RemoveProductDialog
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransferOrderForm(
    contactViewModel: ContactViewModel,
    transferOrderViewModel: TransferOrderViewModel,
    branchViewModel: BranchViewModel,
    userViewModel: UserViewModel = viewModel(),
    productViewModel: ProductViewModel,
    back: () -> Unit
) {
    val transferredProductsViewModel: TransferredProductsViewModel = viewModel()
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val branches = branchViewModel.getAll().observeAsState(listOf())
    var showProductDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToRemove: Product? = null
    var oldBranchId by remember { mutableStateOf(branches.value.firstOrNull()?.id) }
    var destinationBranchId by remember { mutableStateOf(branches.value.filterNot { it.id == oldBranchId }.firstOrNull()?.id) }
    var firstTextFieldValue by remember { mutableStateOf(branches.value.firstOrNull()?.name ?: "No Branches Found") }
    var secondTextFieldValue by remember { mutableStateOf(branches.value.filterNot { it.id == oldBranchId }.firstOrNull()?.name ?: "No Branches Found") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("Add " + stringResource(R.string.transfer_order)).uppercase()) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(enabled = branches.value.size > 1 && transferredProductsViewModel.transfers.isNotEmpty(),
                        onClick = {
                            transferOrderViewModel.insert(
                                TransferOrder(
                                    date = LocalDate.now().toString(),
                                    status = Status.WAITING,
                                    oldBranchId = oldBranchId!!,
                                    destinationBranchId = destinationBranchId!!,
                                    products = transferredProductsViewModel.transfers.associateBy { product -> "Item ${transferredProductsViewModel.transfers.indexOf(product)}" }
                                )
                            )
                            userViewModel.log(event = "create_transfer_order")
                            back.invoke()
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
        Column(
            modifier = Modifier
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (transferredProductsViewModel.transfers.isEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), label = {
                                Text(text = "Transfer Items from this branch")
                            }, modifier = Modifier.fillMaxWidth(), value = firstTextFieldValue, readOnly = true, onValueChange = {  })
                            DropdownMenu(modifier = Modifier
                                .exposedDropdownSize()
                                .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                                branches.value.forEach {
                                        branch ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(text = branch.name) },
                                        onClick = {
                                            oldBranchId = branch.id
                                            firstTextFieldValue = branch.name
                                            expanded = false

                                            if (oldBranchId == destinationBranchId) {
                                                destinationBranchId = branches.value.filterNot { it.id == oldBranchId }.firstOrNull()?.id
                                                secondTextFieldValue = branches.value.filterNot { it.id == oldBranchId }.firstOrNull()?.name ?: "No Branches Found"
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(modifier = Modifier.fillMaxWidth(), label = {
                            Text(
                                text = "Transfer Items from this branch"
                            )
                        }, value = firstTextFieldValue, enabled = false, readOnly = true, onValueChange = {  })
                    }

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), label = {
                            Text(text = "Transfer Items from this branch")
                        }, modifier = Modifier.fillMaxWidth(), value = secondTextFieldValue, readOnly = true, onValueChange = {  })
                        DropdownMenu(modifier = Modifier
                            .exposedDropdownSize()
                            .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                            branches.value.filterNot { it.id == oldBranchId }.forEach {
                                    branch ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(text = branch.name) },
                                    onClick = {
                                        destinationBranchId = branch.id
                                        secondTextFieldValue = branch.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                androidx.compose.material3.ListItem(
                    headlineContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = "Product",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            androidx.compose.material3.Text(text = "Quantity")
                        }
                    },
                    trailingContent = {
                        Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                    },
                    tonalElevation = 5.dp
                )

                Divider()

                LazyColumn {
                    itemsIndexed(transferredProductsViewModel.transfers) { _, product ->
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
                    transferredProductsViewModel.transfers.add(
                        Product(
                            id = id,
                            quantity = quantity,
                            supplier = supplier
                        )
                    ); showProductDialog = false
                },
                branchId = oldBranchId,
                products = products.filter { it.key !in transferredProductsViewModel.transfers.map { products -> products.id } })
        }

        if (showDeleteDialog) {
            RemoveProductDialog(productName = productViewModel.getProduct(productToRemove!!.id)!!.productName, dismissRequest = { showDeleteDialog = false }) {
                transferredProductsViewModel.transfers.remove(productToRemove)
                showDeleteDialog = false
            }
        }
    }
}