package com.example.capstoneproject.product_management.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.ui.theme.Purple500
import kotlinx.coroutines.CoroutineScope

@Composable
fun CategoryScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    viewModel: CategoryViewModel,
    productViewModel: ProductViewModel
) {
    val categories = viewModel.getAll().observeAsState(listOf())
    val state by viewModel.result.collectAsState()
    var category = Category()
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
            FloatingActionButton(onClick = { showDialog = true; category = Category() }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (viewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    val size = categories.value.size
                    Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered categories"; 1 -> "1 category is entered"; else -> "$size categories are entered"})
                }

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
            ConfirmDeletion(item = category.categoryName, onCancel = { showDeleteDialog = false }) {
                viewModel.delete(category)
                productViewModel.removeCategory(categoryId = category.id)
                showDeleteDialog = false
            }
        }

        LaunchedEffect(key1 = state.result, state.errorMessage) {
            if (!state.result && state.errorMessage != null) {
                scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
                viewModel.resetMessage()
            } else if (state.result) {
                scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                viewModel.resetMessage()
            }
        }
    }
}

@Composable
fun CategoryListItem(category: String = "Category", edit: () -> Unit, delete: () -> Unit) {
    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), leadingContent = { Box(modifier = Modifier
        .size(50.dp)
        .background(color = Purple500, shape = CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Filled.Bookmark, contentDescription = null, tint = Color.White) } }, headlineContent = { Text(text = category, fontWeight = FontWeight.Bold) }, trailingContent = {
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
fun CategoryDialog(category: Category, onConfirm: (Category) -> Unit, onCancel: () -> Unit) {
    var categoryName by remember { mutableStateOf(category.categoryName) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(id = R.string.category), fontSize = 24.sp)
        },
        text = {
            Column {
                Text(text = "", fontSize = 1.sp)
                OutlinedTextField(value = categoryName, onValueChange = { categoryName = it }, placeholder = { Text(text = "Enter Category") })
            }
        },
        confirmButton = {
            Button(enabled = categoryName.isNotBlank(), onClick = {
                onConfirm.invoke(category.copy(categoryName = categoryName))
            }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        }
    )
}