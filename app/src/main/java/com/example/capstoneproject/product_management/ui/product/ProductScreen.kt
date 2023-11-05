package com.example.capstoneproject.product_management.ui.product

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StackedBarChart
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.misc.ImageNotAvailable
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProductScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    branchViewModel: BranchViewModel,
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    add: () -> Unit,
    set: (String) -> Unit,
    edit: (String) -> Unit,
    view: (String) -> Unit
) {
    val branch = branchViewModel.getAll().observeAsState(listOf())
    val categories = categoryViewModel.getAll().observeAsState(listOf())
    val products = productViewModel.getAll()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pair: Pair<String, Product>? = null
    var page by rememberSaveable { mutableStateOf(0) }
    val loading = productViewModel.isLoading.value || branchViewModel.isLoading.value || categoryViewModel.isLoading.value

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.product), scope = scope, scaffoldState = scaffoldState) {
                IconButton(onClick = {  }) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { add.invoke() }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (loading) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                TabLayout(tabs = branch.value, selectedTab = page, products = products.values.toList(), productUpdate = productViewModel.update.value) { page = it }

                ProductScreenContent(branchId = if (page == 0) "Default" else branch.value[page - 1].id, categories = categories.value, products = products, productUpdate = productViewModel.update.value, edit = { edit.invoke(it) }, set = { set.invoke(it) }, view = { view.invoke(it) }, delete = {
                    pair = it
                    showDeleteDialog = true
                })
            }
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = pair!!.second.productName, onCancel = { showDeleteDialog = false }) {
                productViewModel.delete(key = pair!!.first)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun TabLayout(tabs: List<Branch>, selectedTab: Int, products: List<Product>, productUpdate: Boolean, onClick: (Int) -> Unit) {
    val defaultMap = remember(tabs) { products.filter { product -> product.stock.values.sum() <= product.criticalLevel } }

    ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp, modifier = Modifier
        .height(50.dp)
        .fillMaxWidth()) {
        Tab(selected = selectedTab == 0, onClick = { onClick.invoke(0) }, text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "All")
                if (defaultMap.isNotEmpty()) {
                    Text(
                        text = defaultMap.count().toString(),
                        color = Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .height(IntrinsicSize.Min)
                            .aspectRatio(1f)
                            .background(MaterialTheme.colors.error)
                    )
                }
            }
        })

        tabs.forEachIndexed {
                index, tab ->
            val map = remember(productUpdate) {
                products.filter { product ->
                    (product.stock[tab.id] ?: 0) <= product.criticalLevel
                }
            }

            Tab(selected = selectedTab == index + 1, onClick = { onClick.invoke(index + 1) }, text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(modifier = Modifier.widthIn(max = 150.dp), text = tab.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (map.isNotEmpty()) {
                        Text(text = map.count().toString(),
                            color = Color.White,
                            modifier = Modifier
                                .clip(CircleShape)
                                .height(IntrinsicSize.Min)
                                .aspectRatio(1f)
                                .background(MaterialTheme.colors.error))
                    }
                }
            })
        }
    }
}

@Composable
fun ProductScreenContent(branchId: String, categories: List<Category>, products: Map<String, Product>, productUpdate: Boolean, edit: (String) -> Unit, set: (String) -> Unit, delete: (Pair<String, Product>) -> Unit, view: (String) -> Unit) {
    val critical = remember(branchId, productUpdate) {
        derivedStateOf {
            products.filterValues {
                it.criticalLevel >= if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0
            }.toList()
        }
    }

    val default = remember(branchId, productUpdate) {
        derivedStateOf {
            products.filterValues {
                it.category !in categories.map { category -> category.id } && it.criticalLevel < if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0
            }.toList()
        }
    }

    val productsInCategories = remember(branchId, productUpdate) {
        val map = mutableMapOf<Category, List<Pair<String, Product>>>()
        for (category in categories) {
            val list = products.filterValues {
                it.category == category.id && it.criticalLevel < (if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0)
            }.toList()

            if (list.isEmpty()) {
                continue
            }

            map[category] = list
        }

        derivedStateOf {
            Log.d("Map", map.values.toString())
            map
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (products.isEmpty()) {
            item {
                Text(modifier = Modifier.padding(16.dp), text = "There are no entered products")
            }
        } else {
            if (critical.value.isNotEmpty()) {
                item {
                    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), headlineContent = {
                        Text(text = "Under Critical Level", fontWeight = FontWeight.Bold)
                    })
                }

                itemsIndexed(critical.value) {
                        _, it ->
                    Log.d("id", products.toString())
                    Products(product = it.second, quantity = if (branchId == "Default") it.second.stock.values.sum() else it.second.stock[branchId] ?: 0, edit = { edit.invoke(it.first) }, set = { set.invoke(it.first) }, delete = { delete.invoke(it) }, view = { view.invoke(it.first) })
                }
            }

            if (default.value.isNotEmpty()) {
                item {
                    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), headlineContent = {
                        Text(text = "No Category")
                    })
                }

                itemsIndexed(default.value) {
                        _, it ->
                    Log.d("id", products.toString())
                    Products(product = it.second, quantity = if (branchId == "Default") it.second.stock.values.sum() else it.second.stock[branchId] ?: 0, edit = { edit.invoke(it.first) }, set = { set.invoke(it.first) }, delete = { delete.invoke(it) }, view = { view.invoke(it.first) })
                }
            }

            for (category in productsInCategories.value) {
                item {
                    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), headlineContent = {
                        Text(text = category.key.categoryName)
                    })
                }

                itemsIndexed(category.value) {
                        _, it ->
                    Log.d("id", products.toString())
                    Products(product = it.second, quantity = if (branchId == "Default") it.second.stock.values.sum() else it.second.stock[branchId] ?: 0, edit = { edit.invoke(it.first) }, set = { set.invoke(it.first) }, delete = { delete.invoke(it) }, view = { view.invoke(it.first) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun Products(product: Product, quantity: Int, edit: () -> Unit, set: () -> Unit, delete: () -> Unit, view: () -> Unit) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), leadingContent = { SubcomposeAsyncImage(error = { ImageNotAvailable(modifier = Modifier.background(Color.LightGray)) },  model = product.image, contentScale = ContentScale.Crop, modifier = Modifier
        .clip(RoundedCornerShape(5.dp))
        .size(50.dp), loading = { CircularProgressIndicator() }, contentDescription = null) }, headlineContent = { Text(text = "Qty: $quantity", fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = product.productName, maxLines = 1, overflow = TextOverflow.Ellipsis) }, trailingContent = {
        IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Product") }, onClick = { expanded = false; edit.invoke() })
            androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.StackedBarChart, contentDescription = null) }, text = { Text(text = "Adjust Quantity") }, onClick = { expanded = false; set.invoke() })
            androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Product") }, onClick = { expanded = false; delete.invoke() })
        }
    }, modifier = Modifier.clickable { view.invoke() })
}