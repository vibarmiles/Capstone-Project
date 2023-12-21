package com.example.capstoneproject.product_management.ui.category

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Block
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ArchiveEntry
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.global.ui.misc.MakeInactiveDialog
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.ui.theme.Purple500
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Composable
fun CategoryScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    viewModel: CategoryViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel
) {
    val categories = viewModel.getAll().observeAsState(listOf())
    val categoriesList = remember(categories.value) {
        categories.value.sortedBy { it.categoryName.uppercase() }
    }
    val state by viewModel.result.collectAsState()
    var category = Category()
    val size = categories.value.size
    var showDialog by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val jsonUriLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(), onResult = {
        if (it != null) {
            val item = context.contentResolver.openInputStream(it)
            if (item != null) {
                category = viewModel.readFromJson(item)

                runBlocking {
                    withContext(Dispatchers.IO) {
                        item.close()
                    }
                }

                showDialog = true
            }
        }
    })

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.category), scope = scope, scaffoldState = scaffoldState) {
                IconButton(onClick = {
                    jsonUriLauncher.launch("application/json")
                }) {
                    Icon(imageVector = Icons.Filled.Upload, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true; category = Category() }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { paddingValues ->
        if (viewModel.isLoading.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column {
                Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered categories"; 1 -> "1 category is entered"; else -> "$size categories are entered"})
                Divider()
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(
                        items = categoriesList,
                        key = {
                            it.id
                        }
                    ) { item ->
                        Column {
                            CategoryListItem(item, edit = {
                                showDialog = true
                                category = item
                            }, archive = {
                                viewModel.archiveItem(category = item, remove = it)
                            }) {
                                showDeleteDialog = true
                                category = item
                            }
                            Divider()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }

        if (showDialog) {
            CategoryDialog(category = category, onConfirm = {
                viewModel.insert(it.copy(active = true))
                userViewModel.log(event = "${if (category.id == "") "add" else "edit"}_product")
                showDialog = false
            }) {
                showDialog = false
            }
        }

        if (showDeleteDialog) {
            MakeInactiveDialog(item = category.categoryName, onCancel = { showDeleteDialog = false }, function = if (category.active) "Inactive" else "Active") {
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
fun CategoryListItem(
    category: Category,
    edit: () -> Unit,
    archive: (Boolean) -> Unit,
    delete: () -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    androidx.compose.material3.ListItem(
        modifier = Modifier.clickable { edit.invoke() },
        colors = ProjectListItemColors(),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Purple500, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        headlineContent = { Text(
            text = category.categoryName,
            fontWeight = FontWeight.Bold,
            color = if (category.active) MaterialTheme.colors.onSurface else MaterialTheme.colors.error
        ) },
        trailingContent = {
            IconButton(
                onClick = { expanded = !expanded },
                content = {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null
                    )
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    leadingIcon = {
                        Icon(imageVector = if (category.active) Icons.Outlined.Block else Icons.Default.Add, contentDescription = null)
                    },
                    text = { Text(text = if (category.active) "Set Inactive" else "Set Active") },
                    onClick = { expanded = false; delete.invoke() }
                )
                ArchiveEntry(name = category.categoryName, isActive = category.active) {
                    archive.invoke(it)
                    expanded = false
                }
            }
        }
    )
}

@Composable
fun CategoryDialog(
    category: Category,
    onConfirm: (Category) -> Unit,
    onCancel: () -> Unit
) {
    var categoryName by remember { mutableStateOf(category.categoryName) }
    val localFocusManager = LocalFocusManager.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(id = R.string.category))
        },
        text = {
            Column {
                Text(text = "", fontSize = 1.sp)
                OutlinedTextField(
                    value = categoryName,
                    colors = GlobalTextFieldColors(),
                    onValueChange = { categoryName = it },
                    placeholder = { Text(text = "Enter Category Name") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        localFocusManager.clearFocus()
                    })
                )
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
        },
        icon = {
            Icon(imageVector = Icons.Outlined.AddCircleOutline, contentDescription = null)
        }
    )
}