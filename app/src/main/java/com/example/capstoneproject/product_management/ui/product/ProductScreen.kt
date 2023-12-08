package com.example.capstoneproject.product_management.ui.product

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ImageNotAvailable
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
import java.time.ZoneId

enum class Direction {
    LEFT, RIGHT
}

@Composable
fun ProductScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    branchViewModel: BranchViewModel,
    contactViewModel: ContactViewModel,
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    userViewModel: UserViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val branch = branchViewModel.getAll().observeAsState(listOf())
    val categories = categoryViewModel.getAll().observeAsState(listOf())
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val direction = remember { mutableStateOf(Direction.LEFT) }
    val state by productViewModel.result.collectAsState()
    var page by rememberSaveable { mutableStateOf(0) }
    val loading = productViewModel.isLoading.value || branchViewModel.isLoading.value || categoryViewModel.isLoading.value
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.product), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            if (userAccountDetails.value.userLevel != UserLevel.Employee) {
                FloatingActionButton(onClick = { add.invoke() }) {
                    Icon(Icons.Filled.Add, null)
                }
            }
        }
    ) { paddingValues ->
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
                if (userAccountDetails.value.userLevel != UserLevel.Employee) {
                    TabLayout(
                        tabs = branch.value.sortedBy { it.name.uppercase() },
                        selectedTab = page,
                        products = products.values.toList(),
                        productUpdate = productViewModel.update.value
                    ) { page = it }
                }

                ProductScreenContent(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(onDragEnd = {
                                when (direction.value) {
                                    Direction.LEFT -> {
                                        if (page - 1 >= 0) {
                                            page -= 1
                                        }
                                    }
                                    Direction.RIGHT -> {
                                        if (page + 1 <= branch.value.size) {
                                            page += 1
                                        }
                                    }
                                }
                            }) { change, dragAmount ->
                                change.consume()

                                if (dragAmount > 50) {
                                    direction.value = Direction.LEFT
                                } else if (dragAmount < -50) {
                                    direction.value = Direction.RIGHT
                                }
                            }
                        },
                    suppliers = suppliers.value,
                    branchId = if (userAccountDetails.value.userLevel == UserLevel.Employee) {
                    userAccountDetails.value.branchId ?: ""
                    } else {
                        if (page == 0) "Default" else branch.value.sortedBy { it.name.uppercase() }[page - 1].id
                    },
                    categories = categories.value.sortedBy { it.categoryName.uppercase() },
                    products = products.filterValues { if (userAccountDetails.value.userLevel != UserLevel.Employee) true else it.isActive }.toList().sortedBy { it.second.productName.uppercase() }.toMap(),
                    productUpdate = productViewModel.update.value,
                    view = { view.invoke(it) },
                    numberOfBranches = branch.value.size.let { if (it == 0) 1 else it }
                )
            }
        }

        LaunchedEffect(key1 = state.result, state.errorMessage) {
            if (!state.result && state.errorMessage != null) {
                productViewModel.resetMessage()
                scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
            } else if (state.result) {
                productViewModel.resetMessage()
                scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
            }
        }
    }
}

@Composable
fun TabLayout(
    tabs: List<Branch>,
    selectedTab: Int,
    products: List<Product>,
    productUpdate: Boolean,
    onClick: (Int) -> Unit
) {
    val defaultMap = remember(tabs) { products.filter { product -> product.isActive }.filter { product -> product.stock.values.sum() <= getCriticalLevel(product = product) } }

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 0.dp,
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
    ) {
        Tab(selected = selectedTab == 0, onClick = { onClick.invoke(0) }, text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "All")
                if (defaultMap.isNotEmpty()) {
                    Badge {
                        Text(text = defaultMap.count().toString(), color = MaterialTheme.colors.surface)
                    }
                }
            }
        })

        tabs.forEachIndexed { index, tab ->
            val map = remember(productUpdate) {
                products.filter { product ->
                    (product.stock[tab.id] ?: 0) <= (getCriticalLevel(product = product) / tabs.size)
                }
            }

            Tab(selected = selectedTab == index + 1, onClick = { onClick.invoke(index + 1) }, text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(modifier = Modifier.widthIn(max = 150.dp), text = tab.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (map.isNotEmpty()) {
                        Badge {
                            Text(text = map.count().toString(), color = MaterialTheme.colors.surface)
                        }
                    }
                }
            })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductScreenContent(
    modifier: Modifier,
    branchId: String,
    categories: List<Category>,
    products: Map<String, Product>,
    suppliers: List<Contact>,
    productUpdate: Boolean,
    numberOfBranches: Int,
    view: (String) -> Unit
) {
    val textFieldValue = remember { mutableStateOf("") }
    val search = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val localFocusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val isFocused = remember { mutableStateOf(false) }
    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex != 0 } }

    if (firstVisible && isFocused.value) {
        localFocusManager.clearFocus()
        isFocused.value = false
    }

    val productsFiltered = remember(branchId, productUpdate, search.value) {
        products.filter { product ->
            search.value.let {
                val supplier = suppliers.firstOrNull { contact -> contact.id == product.value.supplier }?.name?.contains(it, true) ?: false
                val name = product.value.productName.contains(it, true)
                Log.e("SEARCH", "$name & $supplier & ${name || supplier}: ${product.value.productName}")
                name || supplier
            }
        }
    }

    val critical = remember(productsFiltered, productUpdate, branchId) {
        productsFiltered.filterValues {
            (getCriticalLevel(product = it) / if (branchId == "Default") 1 else numberOfBranches) >= if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0
        }.toList()
    }

    val reorder = remember(productsFiltered, productUpdate, branchId) {
        productsFiltered.filterValues {
            ((getReorderPoint(product = it) / if (branchId == "Default") 1 else numberOfBranches) >= if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0) && ((getCriticalLevel(product = it) / if (branchId == "Default") 1 else numberOfBranches) < if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0)
        }.toList()
    }

    val productsInCategories = remember(productsFiltered, productUpdate, branchId) {
        productsFiltered.filterValues {
            (getReorderPoint(product = it) / if (branchId == "Default") 1 else numberOfBranches) < (if (branchId == "Default") it.stock.values.sum() else it.stock[branchId] ?: 0)
        }.toList().groupBy {
            categories.firstOrNull { category -> it.second.category == category.id }?.categoryName ?: "No Category"
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
        if (products.isEmpty()) {
            item {
                Text(modifier = Modifier.padding(16.dp), text = "There are no entered products")
            }
        } else {
            item {
                Column(modifier = Modifier
                    .background(color = MaterialTheme.colors.surface)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                    }
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                    OutlinedTextField(
                        trailingIcon = {
                            if (textFieldValue.value.isNotEmpty()) {
                                IconButton(onClick = {
                                    textFieldValue.value = ""
                                    search.value = ""
                                    localFocusManager.clearFocus()
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        label = { Text(text = "Enter product or supplier name", color = MaterialTheme.colors.onSurface) },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        value = textFieldValue.value,
                        onValueChange = { textFieldValue.value = it },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            search.value = textFieldValue.value
                            localFocusManager.clearFocus()
                        })
                    )
                }
            }

            if (critical.isNotEmpty()) {
                stickyHeader {
                    Column(modifier = Modifier
                        .background(color = MaterialTheme.colors.surface)
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text(text = "Under Critical Level", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary)
                    }
                    Divider()
                }

                items(
                    items = critical,
                    key = {
                        it.first
                    }
                ) {
                    Log.d("id", products.toString())
                    Column {
                        Products(product = it.second, supplier = suppliers.firstOrNull { supplier -> supplier.id == it.second.supplier }?.name ?: "Unknown Supplier", quantity = if (branchId == "Default") it.second.stock.values.sum() else it.second.stock[branchId] ?: 0, view = { view.invoke(it.first) })
                        Divider()
                    }
                }
            }

            if (reorder.isNotEmpty()) {
                stickyHeader {
                    Column(modifier = Modifier
                        .background(color = MaterialTheme.colors.surface)
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text(text = "Under Reorder Point", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary)
                        Divider()
                    }
                }

                items(
                    items = reorder,
                    key = {
                        it.first
                    }
                ) {
                    Log.d("id", products.toString())
                    Column {
                        Products(product = it.second, supplier = suppliers.firstOrNull { supplier -> supplier.id == it.second.supplier }?.name ?: "Unknown Supplier", quantity = if (branchId == "Default") it.second.stock.values.sum() else it.second.stock[branchId] ?: 0, view = { view.invoke(it.first) })
                        Divider()
                    }
                }
            }

            if (productsInCategories.isNotEmpty()) {
                productsInCategories.toList().sortedWith(
                    comparator = compareBy<Pair<String, List<Pair<String, Product>>>> { it.first != "No Category" }.thenBy { it.first.uppercase() }).forEach {
                    stickyHeader {
                        Column(modifier = Modifier
                            .background(color = MaterialTheme.colors.surface)
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Text(text = it.first, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary)
                        }
                        Divider()
                    }

                    items(
                        items = it.second.sortedBy { product -> product.second.productName },
                        key = { id ->
                            id.first
                        }
                    ) {
                        Log.d("id", products.toString())
                        Column {
                            Products(product = it.second, supplier = suppliers.firstOrNull { supplier -> supplier.id == it.second.supplier }?.name ?: "Unknown Supplier", quantity = if (branchId == "Default") it.second.stock.values.sum() else it.second.stock[branchId] ?: 0, view = { view.invoke(it.first) })
                            Divider()
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun Products(
    product: Product,
    quantity: Int,
    supplier: String,
    view: () -> Unit
) {
    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), leadingContent = { SubcomposeAsyncImage(error = { ImageNotAvailable(modifier = Modifier.background(Color.LightGray)) },  model = product.image, contentScale = ContentScale.Crop, modifier = Modifier
        .clip(RoundedCornerShape(5.dp))
        .size(50.dp), loading = { CircularProgressIndicator() }, contentDescription = null) }, headlineContent = { Text(text = product.productName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = supplier, maxLines = 1, overflow = TextOverflow.Ellipsis) }, trailingContent = {
            Text(text =  "$quantity Units", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }, modifier = Modifier.clickable { view.invoke() })
}

fun getCriticalLevel(
    product: Product
): Double {
    val safetyStock = ((product.transaction.monthlySales.values.maxOrNull() ?: 0).toDouble() - (product.transaction.monthlySales.values.let {
        if (it.minOrNull() != null) {
            if (it.min() == it.max()) 0 else it.min()
        } else {
            0
        }
    })) / 2
    return ((product.transaction.monthlySales.values.sum().toDouble() / 12) + safetyStock)
}

fun getReorderPoint(
    product: Product
): Double {
    val lastEditDate = Instant.ofEpochMilli(product.lastEdit as Long).atZone(ZoneId.systemDefault()).toLocalDate()
    val firstStep = product.transaction.monthlySales.values.sum().toDouble() / if (lastEditDate.isLeapYear) 366 else 365
    return (firstStep * product.leadTime) + ((product.transaction.monthlySales.values.maxOrNull() ?: 0) - (product.transaction.monthlySales.values.let {
        if (it.minOrNull() != null) {
            if (it.min() == it.max()) 0 else it.min()
        } else {
            0
        }
    }))
}