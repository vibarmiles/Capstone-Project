package com.example.capstoneproject.product_management.ui.product

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProductScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, branchViewModel: BranchViewModel, productViewModel: ProductViewModel, edit: (String, String, String, Double, String) -> Unit, add: () -> Unit) {
    val branch = branchViewModel.branches.observeAsState(listOf())
    val products = productViewModel.products.observeAsState(listOf())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var product: Product? = null

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
            ProductScreenContent(selectedTabIndex = branchId, products = products.value ?: listOf(), edit = {
                edit.invoke(it.id, it.productName, it.image ?: "", it.price, it.category)
            }) {
                product = it
                showDeleteDialog = true
            }
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = product!!.productName, onCancel = { showDeleteDialog = false }) {
                //viewModel.delete(product = product!!)
                showDeleteDialog = false
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
            Tab(selected = selected == index + 1, onClick = { onClick.invoke(0); selected = index + 1 }, text = { Text(text = tab.name) })
        }
    }
}

@Composable
fun ProductScreenContent(selectedTabIndex: Int, products: List<Product>, edit: (Product) -> Unit, delete: (Product) -> Unit) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

        itemsIndexed(products) {
                _, it ->
            Log.d("id", products.toString())
            Products(it, edit = { edit.invoke(it) }, delete = { delete.invoke(it) })
        }
    }
}

@Composable
fun Products(product: Product, edit: () -> Unit, delete: () -> Unit) {
    androidx.compose.material3.ListItem(leadingContent = { AsyncImage(error = rememberVectorPainter(image = Icons.Filled.Image), model = product.image ?: "", contentScale = ContentScale.Crop, modifier = Modifier
        .clip(RoundedCornerShape(5.dp))
        .size(50.dp), placeholder = rememberVectorPainter(Icons.Default.Image), contentDescription = null) }, headlineContent = { Text(text = "Qty: 0", fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = product.productName) }, trailingContent = { Row {
        IconButton(onClick = edit) {
            Icon(Icons.Filled.Edit, contentDescription = null)
        }
        IconButton(onClick = delete) {
            Icon(Icons.Filled.Delete, contentDescription = null)
        }
    } })
}