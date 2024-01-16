package com.example.capstoneproject.reports_management.ui

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Report
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    productViewModel: ProductViewModel,
    contactViewModel: ContactViewModel,
    branchViewModel: BranchViewModel,
    userAccountDetails: UserAccountDetails,
    view: (String, Int) -> Unit
) {
    val reportsViewModel: ReportsViewModel = viewModel()
    val pagerState = rememberPagerState(0)
    val date = Instant.ofEpochMilli(userAccountDetails.loginDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val branches = branchViewModel.getAll().observeAsState(listOf())
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showBranchDialog by remember { mutableStateOf(false) }
    val productsWithInventoryTurnoverRatio = products
        .map {
            val averageStock = (it.value.transaction.openingStock + it.value.transaction.closingStock).toDouble() / 2
            val sales = productViewModel.getMonthlySales(Instant.ofEpochMilli(userAccountDetails.loginDate).atZone(ZoneId.systemDefault()).toLocalDate(), it.value).sum().toDouble()
            val turnoverRatio = if (averageStock != 0.0) sales / averageStock else 0.0
            turnoverRatio to it
        }
        .sortedWith(compareByDescending<Pair<Double, Map.Entry<String, Product>>> { it.first }.thenBy { it.second.value.productName })
    
    Scaffold(
        topBar = {
            BaseTopAppBar(
                title = "Reports",
                scope = scope,
                scaffoldState = scaffoldState,
                actions = {
                    IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Report, contentDescription = null) }, text = { Text(text = "Generate FSN Analysis") }, onClick = { expanded = false; reportsViewModel.generateFSNReport(products = productsWithInventoryTurnoverRatio) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                            }
                        } })
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null) }, text = { Text(text = "Generate Monthly Sales Report") }, onClick = { expanded = false; reportsViewModel.generateMonthlySalesReport(products = products.values.toList(), date = date) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                            }
                        } })
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Inventory, contentDescription = null) }, text = { Text(text = "Generate Inventory Report") }, onClick = { expanded = false; showBranchDialog = true })
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                ) {
                    Text(text = "FSN Analysis")
                }
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                ) {
                    Text(text = "Monthly Sales")
                }
            }

            HorizontalPager(pageCount = 2, state = pagerState) {
                when (it) {
                    0 -> FSNAnalysis(products = productsWithInventoryTurnoverRatio, suppliers = suppliers.value)
                    1 -> MonthlySales(date = date, products = products.values.toList(), showData = { month, year -> view.invoke(month, year) })
                }
            }
        }

        if (showBranchDialog) {
            BranchDialog(branches = branches.value, onCancel = { showBranchDialog = false }) { listOfBranches ->
                reportsViewModel.generateInventoryReport(products = products.mapValues { it.value.copy(stock = it.value.stock.filterKeys { key -> key in listOfBranches.map { branch -> branch.id } }) }) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                    }
                }
                
                showBranchDialog = false
            }
        }
    }
}

@Composable
fun BranchDialog(
    branches: List<Branch>,
    onCancel: () -> Unit,
    onSubmit: (List<Branch>) -> Unit
) {
    val pairs = remember {
        mutableStateMapOf(*branches.map { it to false}.toTypedArray())
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Select Branches")
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = !pairs.values.contains(false), onCheckedChange = { value -> pairs.putAll(branches.map { it to value}.toTypedArray()) })
                        Text(text = "All Branches")
                    }
                }
                items(branches) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pairs.getOrDefault(it, false), onCheckedChange = { value -> pairs[it] = value })
                        Text(text = it.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit.invoke(pairs.filter { it.value }.keys.toList()) }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent),
                onClick = onCancel,
                enabled = pairs.values.contains(true)
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Inventory, contentDescription = null)
        }
    )
}