package com.example.capstoneproject.product_management.ui.branch

import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.ui.theme.Purple500
import kotlinx.coroutines.CoroutineScope

@Composable
fun BranchScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    viewModel: BranchViewModel,
    productViewModel: ProductViewModel,
    add: () -> Unit,
    edit: (Branch) -> Unit
) {
    val branches by viewModel.getAll().observeAsState(listOf())
    lateinit var branch: Branch
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state by viewModel.result.collectAsState()

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.branch), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (viewModel.isLoading.value) {
            Box(modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    val size = branches.size
                    Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered branches"; 1 -> "1 branch is entered"; else -> "$size branches are entered"})
                }

                itemsIndexed(branches) {
                        _, item ->
                    BranchListItem(branch = item, edit = {
                        edit.invoke(item)
                    }) {
                        branch = item
                        showDeleteDialog = true
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = branch.name, onCancel = { showDeleteDialog = false }) {
                viewModel.delete(branch)
                productViewModel.removeBranchStock(branchId = branch.id)
                showDeleteDialog = false
            }
        }

        LaunchedEffect(state.result, state.errorMessage) {
            Log.d("=============", "Called")
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
fun BranchListItem(
    branch: Branch,
    edit: () -> Unit,
    delete: () -> Unit
) {
    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), headlineContent = { Text(text = branch.name, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis, maxLines = 1) }, supportingContent = { Text(text = branch.address, overflow = TextOverflow.Ellipsis, maxLines = 1) }, trailingContent = {
        Row {
            IconButton(onClick = edit) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = delete) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    }, leadingContent = { Box(modifier = Modifier
        .size(50.dp)
        .background(color = Purple500, shape = CircleShape), contentAlignment = Alignment.Center) { Text(text = branch.name.substring(startIndex = 0, endIndex = 1).uppercase(), fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center) } } )
}