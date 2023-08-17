package com.example.capstoneproject.product_management.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.composable.BaseTopAppBar
import kotlinx.coroutines.CoroutineScope

@Composable
fun CategoryScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, add: () -> Unit, edit: (String) -> Unit) {
    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.category), scope = scope, scaffoldState = scaffoldState)
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
                CategoryListItem() {
                    
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(category: String = "Category", edit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(50.dp))
        Text(text = category, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 8.dp))
        IconButton(onClick = edit) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
    }
}