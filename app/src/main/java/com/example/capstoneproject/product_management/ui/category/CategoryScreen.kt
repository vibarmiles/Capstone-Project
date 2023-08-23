package com.example.capstoneproject.product_management.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.product_management.data.Room.category.Category
import com.example.capstoneproject.product_management.ui.category.viewmodel.CategoryViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun CategoryScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: CategoryViewModel) {
    val categories = viewModel.categories.collectAsState(listOf())
    var category: Category? = null
    var showDialog by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.category), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true; category = null }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
        it -> it
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(categories.value) {
                _, item ->
                CategoryListItem(item.categoryName, edit = {
                    showDialog = true
                    category = item
                }) {
                    showDeleteDialog = true
                    category = item
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        if (showDialog) {
            CategoryDialog(category = category, onConfirm = {
                viewModel.insert(it)
                showDialog = false
            }) {
                showDialog = false
            }
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = category?.categoryName ?: "", onCancel = { showDeleteDialog = false }) {
                viewModel.delete(category!!)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun CategoryListItem(category: String = "Category", edit: () -> Unit, delete: () -> Unit) {
    androidx.compose.material3.ListItem(leadingContent = { Icon(Icons.Outlined.Bookmark, contentDescription = null, modifier = Modifier.size(50.dp)) }, headlineContent = { Text(text = category, fontWeight = FontWeight.Bold) }, trailingContent = {
        Row {
            IconButton(onClick = edit) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = delete) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    })
}

@Composable
fun CategoryDialog(category: Category? = null, onConfirm: (Category) -> Unit, onCancel: () -> Unit) {
    var name = ""
    if (category != null) {
        name = category.categoryName
    }
    var categoryName by remember {
        mutableStateOf(name)
    }

    var isValid by remember {
        mutableStateOf(true)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(id = R.string.category), fontSize = 24.sp)
        },
        text = {
            Column {
                Text(text = "", fontSize = 1.sp)
                OutlinedTextField(value = categoryName, onValueChange = { categoryName = it }, placeholder = { Text(text = "Enter Category") }, isError = !isValid, trailingIcon = { if (!isValid) Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color.Red
                )})
            }
        },
        confirmButton = {
            Button(onClick = {
                if (categoryName.isNotBlank()) {
                    isValid = true
                    if (category == null) {
                        onConfirm.invoke(Category(categoryName = categoryName))
                    } else {
                        onConfirm.invoke(Category(id = category.id, categoryName = categoryName))
                    }
                } else {
                    isValid = false
                }
            }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        }
    )
}