package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.ui.branch.viewmodel.BranchViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProductScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: BranchViewModel) {
    val branch = viewModel.branches.collectAsState(listOf())
    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.product), scope = scope, scaffoldState = scaffoldState) {
                IconButton(onClick = {  }) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {  }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
        it
        var index by remember { mutableStateOf(0) }
        Column(modifier = Modifier.fillMaxSize()) {
            TabLayout(tabs = branch) {
                it ->

            }
            Divider(thickness = 1.dp, color = Color.Black)
        }
    }
}

@Composable
fun TabLayout(tabs: State<List<Branch>>, onClick: (Int) -> Unit) {
    LazyRow(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)) {
        item {
            Tab("All") {

            }
        }
        itemsIndexed(tabs.value) {
            index, item ->
            Tab(item.branchName) {
                //index starts at 0
                onClick.invoke(index)
            }
        }
    }
}

@Composable
fun Tab(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxHeight()
        .clickable { onClick.invoke() }
        .defaultMinSize(minWidth = 75.dp)
        .padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}