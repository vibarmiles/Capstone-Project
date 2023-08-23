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
        var index by remember { mutableStateOf(0) }
        val listState = rememberLazyListState()
        Column(modifier = Modifier.fillMaxSize()) {
            TabLayout(tabs = branch.value, selectedTabIndex = index) {
                index = it
                scope.launch { listState.animateScrollToItem(index) }
            }
            ProductScreenContent(pages = branch.value.size, selectedTabIndex = index, lazyListState = listState)
        }
    }
}

@Composable
fun TabLayout(tabs: List<Branch>, selectedTabIndex: Int, onClick: (Int) -> Unit) {
    ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 0.dp, modifier = Modifier
        .height(50.dp)
        .fillMaxWidth()) {
        Tab(selected = selectedTabIndex == 0, onClick = { onClick.invoke(0) }, text = { Text(text = "All") })

        tabs.forEachIndexed {
                index, tab ->
            Tab(selected = selectedTabIndex == index + 1, onClick = { onClick.invoke(index + 1) }, text = { Text(text = tab.branchName) })
        }
    }
}

@Composable
fun ProductScreenContent(pages: Int, selectedTabIndex: Int, lazyListState: LazyListState) {
    LazyRow(state = lazyListState, userScrollEnabled = false) {
        item {
            Box(modifier = Modifier
                .fillMaxHeight()
                .width(LocalConfiguration.current.screenWidthDp.dp)
                .background(color = Color.White))

        }
        for (page in 1..pages) {
            item {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .width(LocalConfiguration.current.screenWidthDp.dp)
                    .background(color = if (page % 2 == 1) Color.Black else Color.White))
            }
        }
    }
}

@Composable
fun Products() {

}