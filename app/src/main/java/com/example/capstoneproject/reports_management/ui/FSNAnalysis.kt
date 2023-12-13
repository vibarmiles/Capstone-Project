package com.example.capstoneproject.reports_management.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FSNAnalysis(
    products: List<Pair<Double, Map.Entry<String, Product>>>,
    suppliers: List<Contact>
) {
    val textFieldValue = remember { mutableStateOf("") }
    val search = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val localFocusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val isFocused = remember { mutableStateOf(false) }
    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex != 0 } }

    val productsFiltered = remember(search.value) {
        products.filter { product ->
            search.value.let {
                val supplier = suppliers.firstOrNull { contact -> contact.id == product.second.value.supplier }?.name?.contains(it, true) ?: false
                val name = product.second.value.productName.contains(it, true)
                Log.e("SEARCH", "$name & $supplier & ${name || supplier}: ${product.second.value.productName}")
                name || supplier
            }
        }
    }

    if (firstVisible && isFocused.value) {
        localFocusManager.clearFocus()
        isFocused.value = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item {
            Column(modifier = Modifier
                .background(color = MaterialTheme.colors.surface)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused.value = it.isFocused
                }
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                OutlinedTextField(
                    trailingIcon = {
                        if (textFieldValue.value.isNotEmpty()) {
                            IconButton(onClick = {
                                textFieldValue.value = ""
                                search.value = ""
                                localFocusManager.clearFocus()
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    label = { Text(text = "Enter product or supplier name", color = MaterialTheme.colors.onSurface) },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    value = textFieldValue.value,
                    onValueChange = { textFieldValue.value = it },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        search.value = textFieldValue.value
                        localFocusManager.clearFocus()
                    })
                )
            }
        }

        stickyHeader {
            Column {
                ListItem(
                    colors = ProjectListItemColors(),
                    headlineContent = {
                        Text(
                            text = "Products",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = {
                        Text(text = "ITR | C")
                    }
                )
                Divider()
            }
        }
        items(
            items = productsFiltered,
            key = {
                  it.second.key
            },
        ) {
            Column {
                ListItem(
                    colors = ProjectListItemColors(),
                    headlineContent = {
                        Text(text = it.second.value.productName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    },
                    supportingContent = {
                        Text(text = suppliers.firstOrNull { supplier -> supplier.id == it.second.value.supplier }?.name ?: "Unknown Supplier")
                    },
                    trailingContent = {
                        Text(
                            text = String.format("%.2f", it.first) + " | " + if (it.first > 3) "F" else if (it.first in 1.0..3.0) "S" else "N",
                            color = if (it.first < 1) MaterialTheme.colors.error else MaterialTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Divider()
            }
        }
    }
}