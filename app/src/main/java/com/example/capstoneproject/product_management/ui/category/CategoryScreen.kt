package com.example.capstoneproject.product_management.ui.category

import android.app.Application
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.Misc.BaseTopAppBar
import com.example.capstoneproject.global.ui.Misc.ConfirmDeletion
import com.example.capstoneproject.product_management.data.Room.category.Category
import com.example.capstoneproject.product_management.ui.category.viewmodel.CategoryViewModel
import com.example.capstoneproject.product_management.ui.category.viewmodel.CategoryViewModelFactory
import kotlinx.coroutines.CoroutineScope

@Composable
fun CategoryScreen(scope: CoroutineScope, scaffoldState: ScaffoldState) {
    val viewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(LocalContext.current.applicationContext as Application))
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
        it
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(categories.value) {
                _, item ->
                CategoryListItem(item.name, edit = {
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
            ConfirmDeletion(item = category?.name ?: "", onCancel = { showDeleteDialog = false }) {
                viewModel.delete(category!!)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun CategoryListItem(category: String = "Category", edit: () -> Unit, delete: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Bookmark, contentDescription = null, modifier = Modifier.size(50.dp))
        Text(text = category, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 8.dp))
        IconButton(onClick = edit) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
        IconButton(onClick = delete) {
            Icon(Icons.Filled.Delete, contentDescription = null)
        }
    }
}

@Composable
fun CategoryDialog(category: Category? = null, onConfirm: (Category) -> Unit, onCancel: () -> Unit) {
    var name = ""
    if (category != null) {
        name = category.name
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
                OutlinedTextField(value = categoryName, onValueChange = { categoryName = it }, placeholder = { Text(text = "Enter Category") }, colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedBorderColor = if (isValid) Color.Black else Color.Red, focusedBorderColor = if (isValid) Color.Black else Color.Red), isError = !isValid, trailingIcon = { if (!isValid) Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color.Red
                )})
                Text(text = "", fontSize = 4.sp)
                if (!isValid) {
                    Text(text = "Invalid Category!", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (categoryName.isNotBlank()) {
                    isValid = true
                    if (category == null) {
                        onConfirm.invoke(Category(name = categoryName))
                    } else {
                        onConfirm.invoke(Category(id = category.id, name = categoryName))
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