package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.ui.product.viewModel.ProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: ProductViewModel, add: () -> Unit) {
    val branch = viewModel.branches.collectAsState(listOf())
    val category = viewModel.categories.collectAsState(listOf())

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.product), scope = scope, scaffoldState = scaffoldState) {
                IconButton(onClick = {  }) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            it -> it
        var branchId by rememberSaveable { mutableStateOf(0) }
        Column(modifier = Modifier.fillMaxSize()) {
            TabLayout(tabs = branch.value) {
                branchId = it
            }
            ProductScreenContent(selectedTabIndex = branchId)
        }
    }
}

@Composable
fun TabLayout(tabs: List<Branch>, onClick: (Int) -> Unit) {
    var selected by remember { mutableStateOf(0) }
    ScrollableTabRow(selectedTabIndex = selected, edgePadding = 0.dp, modifier = Modifier
        .height(50.dp)
        .fillMaxWidth()) {
        Tab(selected = selected == 0, onClick = { onClick.invoke(0); selected = 0 }, text = { Text(text = "All") })

        tabs.forEachIndexed {
                index, tab ->
            Tab(selected = selected == index + 1, onClick = { onClick.invoke(tab.id); selected = index + 1 }, text = { Text(text = tab.branchName) })
        }
    }
}

@Composable
fun ProductScreenContent(selectedTabIndex: Int) {

}

@Composable
fun Products() {

}