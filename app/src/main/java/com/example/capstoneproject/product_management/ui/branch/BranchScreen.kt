package com.example.capstoneproject.product_management.ui.branch

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Block
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ArchiveEntry
import com.example.capstoneproject.global.ui.misc.MakeInactiveDialog
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
    val branches = viewModel.getAll().observeAsState(listOf())
    val branchesList = remember(branches.value) {
        branches.value.sortedBy { it.name.uppercase() }
    }
    lateinit var branch: Branch
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state by viewModel.result.collectAsState()
    val size = branchesList.size
    val context = LocalContext.current

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
            Column {
                Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered branches"; 1 -> "1 branch is entered"; else -> "$size branches are entered"})
                Divider()
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(
                        items = branchesList,
                        key = {
                            it.id
                        }
                    ) { item ->
                        Column {
                            BranchListItem(branch = item, edit = {
                                edit.invoke(item)
                            }, archive = {
                                viewModel.archiveItem(branch = item, remove = it)
                            }) {
                                branch = item
                                showDeleteDialog = true
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

        if (showDeleteDialog) {
            MakeInactiveDialog(item = branch.name, onCancel = { showDeleteDialog = false }, function = if (branch.active) "Inactive" else "Active") {
                viewModel.delete(branch)
                productViewModel.removeBranchStock(branchId = branch.id)
                showDeleteDialog = false
            }
        }

        LaunchedEffect(state.result, state.errorMessage) {
            if (!state.result && state.errorMessage != null) {
                Toast.makeText(context, state.errorMessage!!, Toast.LENGTH_SHORT).show()
                viewModel.resetMessage()
            } else if (state.result) {
                Toast.makeText(context, "Successfully Done!", Toast.LENGTH_SHORT).show()
                viewModel.resetMessage()
            }
        }
    }
}

@Composable
fun BranchListItem(
    branch: Branch,
    edit: () -> Unit,
    archive: (Boolean) -> Unit,
    delete: () -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    androidx.compose.material3.ListItem(
        modifier = Modifier.clickable { edit.invoke() },
        colors = ProjectListItemColors(),
        headlineContent = {
            Text(
                text = branch.name,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = if (branch.active) MaterialTheme.colors.onSurface else MaterialTheme.colors.error
            )
        },
        supportingContent = {
            Text(
                text = branch.address,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
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
                        Icon(imageVector = if (branch.active) Icons.Outlined.Block else Icons.Default.Add, contentDescription = null)
                    },
                    text = { Text(text = if (branch.active) "Set Inactive" else "Set Active") },
                    onClick = { expanded = false; delete.invoke() }
                )
                ArchiveEntry(name = branch.name, isActive = branch.active) {
                    archive.invoke(it)
                    expanded = false
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Purple500, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = branch.name.first().uppercase(),
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}