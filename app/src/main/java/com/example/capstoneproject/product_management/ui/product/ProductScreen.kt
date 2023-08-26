package com.example.capstoneproject.product_management.ui.product

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.data.Room.category.Category
import com.example.capstoneproject.product_management.data.Room.product.Product
import com.example.capstoneproject.product_management.ui.product.viewModel.ProductViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProductScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: ProductViewModel, add: () -> Unit) {
    val branch = viewModel.branches.collectAsState(initial = listOf())
    val category = viewModel.categories.collectAsState(initial = listOf())
    val product = viewModel.products.collectAsState(initial = listOf())
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            ProductScreenContent(selectedTabIndex = branchId, category = category.value, product = product.value) {
                showDeleteDialog = true
            }
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
fun ProductScreenContent(selectedTabIndex: Int, category: List<Category>, product: List<Product>, delete: (Boolean) -> Unit) {
    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(product) {
            _, it ->
            Products(it, edit = {  }, delete = { delete.invoke(true) })
        }
    }
}

@Composable
fun Products(product: Product, edit: (Product) -> Unit, delete: (Product) -> Unit) {
    androidx.compose.material3.ListItem(leadingContent = { AsyncImage(error = rememberVectorPainter(image = Icons.Filled.BrokenImage), model = product.image ?: "", contentScale = ContentScale.Crop, modifier = Modifier
        .clip(RoundedCornerShape(5.dp))
        .size(50.dp), placeholder = rememberVectorPainter(Icons.Default.Image), contentDescription = null) }, headlineContent = { Text(text = "Qty: " + product.quantity.toString(), fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = product.productName) }, trailingContent = { Row {
        IconButton(onClick = { edit.invoke(product) }) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
        IconButton(onClick = { delete.invoke(product) }) {
            Icon(Icons.Filled.Delete, contentDescription = null)
        }
    } })
}