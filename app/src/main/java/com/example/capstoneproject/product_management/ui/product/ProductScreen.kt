package com.example.capstoneproject.product_management.ui.product

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StackedBarChart
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProductScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, branchViewModel: BranchViewModel, productViewModel: ProductViewModel, categoryViewModel: CategoryViewModel, edit: (String, String, String, Double, String) -> Unit, set: (String, String) -> Unit, add: () -> Unit) {
    val branch = branchViewModel.branches.observeAsState(listOf())
    val products = productViewModel.products
    val categories = categoryViewModel.categories.observeAsState(listOf())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pair: Pair<String, String>? = null

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
        var branchId by rememberSaveable { mutableStateOf("Default") }
        Column(modifier = Modifier.fillMaxSize()) {
            TabLayout(tabs = branch.value) {
                branchId = it
            }
            ProductScreenContent(selectedTab = branchId, categories = categories.value, products = products, edit = {
                edit.invoke(it.first, it.second.productName, it.second.image ?: "No Image Selected", it.second.price, it.second.category ?: "None")
            }, set = { id, stock-> set.invoke(id, stock) }) {
                pair = it
                showDeleteDialog = true
            }
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = pair!!.second, onCancel = { showDeleteDialog = false }) {
                productViewModel.delete(key = pair!!.first)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun TabLayout(tabs: List<Branch>, onClick: (String) -> Unit) {
    var selected by remember { mutableStateOf(0) }
    ScrollableTabRow(selectedTabIndex = selected, edgePadding = 0.dp, modifier = Modifier
        .height(50.dp)
        .fillMaxWidth()) {
        Tab(selected = selected == 0, onClick = { onClick.invoke("Default"); selected = 0 }, text = { Text(text = "All") })

        tabs.forEachIndexed {
                index, tab ->
            Tab(selected = selected == index + 1, onClick = { onClick.invoke(tab.id); selected = index + 1 }, text = { Text(text = tab.name) })
        }
    }
}

@Composable
fun ProductScreenContent(selectedTab: String, categories: List<Category>, products: Map<String, Product>, edit: (Pair<String, Product>) -> Unit, set: (String, String) -> Unit, delete: (Pair<String, String>) -> Unit) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

        itemsIndexed(products.filterValues { it.category !in categories.map { category -> category.id } }.toList()) {
                _, it ->
            Log.d("id", products.toString())
            Products(product = it.second, quantity = if (selectedTab == "Default") it.second.stock.values.sum() else it.second.stock[selectedTab] ?: 0, edit = { edit.invoke(it) }, set = { set.invoke(it.first, it.second.stock.toString()) }, delete = { delete.invoke(Pair(it.first, it.second.productName)) })
        }

        for (category in categories) {
            val list = products.filterValues { it.category == category.id }.toList()
            if (list.isEmpty()) {
                continue
            }

            item {
                androidx.compose.material3.ListItem(headlineContent = { Text(text = category.categoryName) }, colors = ListItemDefaults.colors(containerColor = Color.White))
            }
            
            itemsIndexed(list) {
                    _, it ->
                Log.d("id", products.toString())
                Products(product = it.second, quantity = if (selectedTab == "Default") it.second.stock.values.sum() else it.second.stock[selectedTab] ?: 0, edit = { edit.invoke(it) }, set = { set.invoke(it.first, it.second.stock.toString()) }, delete = { delete.invoke(Pair(it.first, it.second.productName)) })
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun Products(product: Product, quantity: Int, edit: () -> Unit, set: () -> Unit, delete: () -> Unit) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    androidx.compose.material3.ListItem(leadingContent = { AsyncImage(error = rememberVectorPainter(image = Icons.Filled.Image), model = product.image ?: "", contentScale = ContentScale.Crop, modifier = Modifier
        .clip(RoundedCornerShape(5.dp))
        .size(50.dp), placeholder = rememberVectorPainter(Icons.Default.Image), contentDescription = null) }, headlineContent = { Text(text = "Qty: $quantity", fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = product.productName) }, trailingContent = {
        IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Product") }, onClick = { expanded = false; edit.invoke() })
            androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.StackedBarChart, contentDescription = null) }, text = { Text(text = "Adjust Quantity") }, onClick = { expanded = false; set.invoke() })
            androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Product") }, onClick = { expanded = false; delete.invoke() })
        }
    })
}