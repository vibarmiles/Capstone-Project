package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StackedBarChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.misc.ImageNotAvailable
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.ui.theme.primaryColor
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewProduct(dismissRequest: () -> Unit, productViewModel: ProductViewModel, contactViewModel: ContactViewModel, categoryViewModel: CategoryViewModel, branchViewModel: BranchViewModel, productId: String, product: Product, edit: () -> Unit, set: () -> Unit, delete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product.productName) },
                navigationIcon = {
                    IconButton(onClick = dismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    var expanded: Boolean by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Product") }, onClick = { expanded = false; edit.invoke() })
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.StackedBarChart, contentDescription = null) }, text = { Text(text = "Adjust Quantity") }, onClick = { expanded = false; set.invoke() })
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Product") }, onClick = { expanded = false; showDeleteDialog = true })
                    }
                }
            )
        }
    ) {
            paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray)) {
                if (product.image == null) {
                    ImageNotAvailable(modifier = Modifier.size(200.dp))
                    Text(text = "NO IMAGE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                } else {
                    SubcomposeAsyncImage(error = { ImageNotAvailable() },  model = product.image ?: "", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), loading = { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { CircularProgressIndicator() } }, contentDescription = null)
                }
            }

            ViewProductTabs(selectedTabIndex = pagerState.currentPage) { coroutineScope.launch { pagerState.animateScrollToPage(it) } }

            HorizontalPager(state = pagerState, pageCount = 2) {
                when (it) {
                    0 -> ViewProductDetails(productId = productId, product = product, supplier = contactViewModel.getAll().observeAsState(listOf()).value.firstOrNull { contact -> contact.id == product.supplier }?.name ?: "Unknown Supplier", category = categoryViewModel.getAll().observeAsState(listOf()).value.firstOrNull { category -> category.id == product.category }?.categoryName ?: "No Category")
                    1 -> ViewProductStock(stock = product.stock, branch = branchViewModel.getAll().observeAsState(listOf()).value)
                }
            }

            if (showDeleteDialog) {
                ConfirmDeletion(item = product.productName, onCancel = { showDeleteDialog = false }) {
                    productViewModel.delete(key = productId)
                    showDeleteDialog = false
                    delete.invoke()
                }
            }
        }
    }
}

@Composable
fun ViewProductTabs(selectedTabIndex: Int, onClick: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()) {
        Tab(selected = selectedTabIndex == 0, onClick = { onClick.invoke(0) }) {
            Text(text = "Product Details")
        }

        Tab(selected = selectedTabIndex == 1, onClick = { onClick.invoke(1) }) {
            Text(text = "Stocks")
        }
    }
}

@Composable
fun ViewProductDetails(productId: String, product: Product, supplier: String, category: String) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = product.productName, fontSize = 24.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        Text(text = "ID: $productId", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Category: $category", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "Supplier: $supplier", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "Purchase Price: ${product.purchasePrice}", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "Selling Price: ${product.sellingPrice} (${String.format("%.99f", ((product.sellingPrice/product.purchasePrice) - 1) * 100).trimEnd('0').trimEnd('.')}%)", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "Critical Level: ${product.criticalLevel}", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ViewProductStock(stock: Map<String, Int>, branch: List<Branch>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            androidx.compose.material3.ListItem(headlineContent = {
                Row {
                    Text(text = "Branch", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Stock", fontWeight = FontWeight.Bold)
                }
            }, tonalElevation = 5.dp)
        }

        itemsIndexed(branch) {
                _, branch ->
            androidx.compose.material3.ListItem(headlineContent = {
                Row {
                    Text(text = branch.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    stock.keys.firstOrNull { it == branch.id }?.let { Text(text = stock[it].toString(), fontWeight = FontWeight.Bold) } ?: run { Text(text = "0") }
                }
            })
        }
    }
}