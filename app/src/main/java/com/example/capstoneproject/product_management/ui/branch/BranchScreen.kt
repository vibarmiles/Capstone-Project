package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Store
import androidx.compose.runtime.*
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
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.ui.branch.viewmodel.BranchViewModel
import com.example.capstoneproject.ui.theme.Purple500
import kotlinx.coroutines.CoroutineScope

@Composable
fun BranchScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: BranchViewModel, add: () -> Unit, edit: (Int, String, String) -> Unit) {
    val branches = viewModel.branches.collectAsState(listOf())
    var branch: Branch? = null
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

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
        it
        LazyColumn(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                val size = branches.value.size
                Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered branches"; 1 -> "1 branch is entered"; else -> "$size branches are entered"})
            }

            itemsIndexed(branches.value) {
                _, item ->
                BranchListItem(branch = item.branchName, address = item.address, edit = {
                    edit.invoke(item.id, item.branchName, item.address)
                }) {
                    branch = item
                    showDeleteDialog = true
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = branch?.branchName ?: "", onCancel = { showDeleteDialog = false }) {
                viewModel.delete(branch!!)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun BranchListItem(branch: String = "Branch", address: String = "#234 Address St., asdfasdfwerwer", edit: () -> Unit, delete: () -> Unit) {
    androidx.compose.material3.ListItem(headlineContent = { Text(text = branch, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis, maxLines = 1) }, supportingContent = { Text(text = address, overflow = TextOverflow.Ellipsis, maxLines = 1) }, trailingContent = {
        Row {
            IconButton(onClick = edit) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = delete) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    }, leadingContent = { Box(modifier = Modifier.size(50.dp).background(color = Purple500, shape = CircleShape), contentAlignment = Alignment.Center) { Text(text = branch.substring(startIndex = 0, endIndex = 1), fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center) } } )
}