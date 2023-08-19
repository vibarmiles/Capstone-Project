package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.Misc.BaseTopAppBar
import kotlinx.coroutines.CoroutineScope

@Composable
fun BranchScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, add: () -> Unit) {
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
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(6) {
                BranchListItem(edit = {  }) {

                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun BranchListItem(branch: String = "Branch", address: String = "#234 Address St., asdfasdfwerwer", edit: () -> Unit, delete: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Store, contentDescription = null, modifier = Modifier.size(50.dp))
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp)) {
            Text(text = branch, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = address, overflow = TextOverflow.Ellipsis, maxLines = 1)
        }
        IconButton(onClick = edit) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
        IconButton(onClick = delete) {
            Icon(Icons.Filled.Delete, contentDescription = null)
        }
    }
}