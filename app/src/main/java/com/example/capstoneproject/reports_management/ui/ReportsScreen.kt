package com.example.capstoneproject.reports_management.ui

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Report
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.ui.POSViewModel
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    productViewModel: ProductViewModel,
    contactViewModel: ContactViewModel,
    branchViewModel: BranchViewModel,
    posViewModel: POSViewModel,
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
    var showDateDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showBranchDialog by remember { mutableStateOf(false) }
    var showActiveListDialog by remember { mutableStateOf(false) }
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
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Report, contentDescription = null) }, text = { Text(text = "Generate FSN Analysis") }, onClick = {
                            expanded = false

                            reportsViewModel.generateFSNReport(products = productsWithInventoryTurnoverRatio) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null) }, text = { Text(text = "Generate Sales Report") }, onClick = { expanded = false; showDateDialog = true })
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Inventory, contentDescription = null) }, text = { Text(text = "Generate Inventory Report") }, onClick = { expanded = false; showBranchDialog = true })
                        androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Phone, contentDescription = null) }, text = { Text(text = "Generate Supplier Master List") }, onClick = { expanded = false; showActiveListDialog = true })
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

        if (showActiveListDialog) {
            ActiveListDialog(onCancel = { showActiveListDialog = false }) {
                reportsViewModel.GenerateSupplierMasterList(suppliers.value.filter { supplier ->
                    supplier.active && it.first() || !supplier.active && it.last()
                }) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                    }
                }

                showActiveListDialog = false
            }
        }

        if (showDateDialog) {
            DatesDialog(date = date, onCancel = { showDateDialog = false }) { report, from, to ->
                when (report) {
                    ReportDateFormat.Day -> {
                        scope.launch {
                            var invoices: List<Invoice>?
                            if (!posViewModel.taken.value) {
                                invoices = posViewModel.getCurrent().value
                                delay(3000)
                                Log.e("Invoice", invoices.toString())
                            }
                            invoices = posViewModel.getCurrent().value
                            reportsViewModel.generateDailySalesReport(invoices = invoices ?: listOf(), fromDate = from, toDate = to) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    ReportDateFormat.Week -> {
                        scope.launch {
                            var invoices: List<Invoice>?
                            if (!posViewModel.taken.value) {
                                invoices = posViewModel.getCurrent().value
                                delay(3000)
                                Log.e("Invoice", invoices.toString())
                            }
                            invoices = posViewModel.getCurrent().value
                            reportsViewModel.generateWeeklySalesReport(invoices = invoices ?: listOf(), fromDate = from, toDate = to) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    ReportDateFormat.Month -> {
                        reportsViewModel.generateMonthlySalesReport(products = products.values.toList(), fromDate = from, toDate = to, date = date) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    ReportDateFormat.Year -> {
                        reportsViewModel.generateYearlySalesReport(products = products.values.toList(), fromDate = from, toDate = to, date = date) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                showDateDialog = false
            }
        }

        if (showBranchDialog) {
            BranchDialog(branches = branches.value, onCancel = { showBranchDialog = false }) { listOfBranches, inactive ->
                reportsViewModel.generateInventoryReport(products = products.filter { it.value.active || !it.value.active == inactive }.mapValues { it.value.copy(stock = it.value.stock.filterKeys { key -> key in listOfBranches.map { branch -> branch.id } }) }) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "File Generated", Toast.LENGTH_SHORT).show()
                    }
                }

                showBranchDialog = false
            }
        }
    }
}

enum class ReportDateFormat {
    Day, Week, Month, Year
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DatesDialog(
    date: LocalDate,
    onCancel: () -> Unit,
    onSubmit: (ReportDateFormat, LocalDate, LocalDate) -> Unit
) {
    val reportDateFormat = remember { mutableStateOf(ReportDateFormat.Month) }
    val fromDate = remember { mutableStateOf(date.minusMonths(11)) }
    val toDate = remember { mutableStateOf(date) }
    val from = remember { mutableStateOf(date.minusMonths(11)) }
    val to = remember { mutableStateOf(date) }
    val fromDateTextFieldValue = remember { mutableStateOf("${fromDate.value.month.value.toString().padStart(2, '0')}/${fromDate.value.dayOfMonth.toString().padStart(2, '0')}/${fromDate.value.year.toString().padStart(4, '0')}") }
    val toDateTextFieldValue = remember { mutableStateOf("${toDate.value.month.value.toString().padStart(2, '0')}/${toDate.value.dayOfMonth.toString().padStart(2, '0')}/${toDate.value.year.toString().padStart(4, '0')}") }
    val isFromDateValid = remember { mutableStateOf(true) }
    val isToDateValid = remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(reportDateFormat.value.name) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Date")
        },
        text = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)) {

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = selectedCategory, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.category)) })

                    DropdownMenu(
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .exposedDropdownSize()
                            .requiredHeightIn(max = 300.dp)
                            .fillMaxWidth(),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        enumValues<ReportDateFormat>().forEach {
                            androidx.compose.material3.DropdownMenuItem(text = {
                                Text(text = it.name) },
                                onClick = {
                                    reportDateFormat.value = it
                                    selectedCategory = it.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    trailingIcon = {
                        if (fromDateTextFieldValue.value.isNotEmpty()) {
                            IconButton(onClick = {
                                fromDateTextFieldValue.value = ""
                                fromDate.value = date
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    label = { Text(text = "From this Date", color = MaterialTheme.colors.onSurface) },
                    placeholder = { Text(text = "mm/dd/yyyy", color = MaterialTheme.colors.onSurface) },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) },
                    value = fromDateTextFieldValue.value,
                    isError = !isFromDateValid.value,
                    onValueChange = {
                        if (it.length <= 10) {
                            fromDateTextFieldValue.value = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                )
                OutlinedTextField(
                    trailingIcon = {
                        if (fromDateTextFieldValue.value.isNotEmpty()) {
                            IconButton(onClick = {
                                toDateTextFieldValue.value = ""
                                toDate.value = date.minusMonths(11)
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    label = { Text(text = "To this Date", color = MaterialTheme.colors.onSurface) },
                    placeholder = { Text(text = "mm/dd/yyyy", color = MaterialTheme.colors.onSurface) },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isToDateValid.value,
                    leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) },
                    value = toDateTextFieldValue.value,
                    onValueChange = {
                        if (it.length <= 10) {
                            toDateTextFieldValue.value = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newFromDate = fromDateTextFieldValue.value
                val newToDate = toDateTextFieldValue.value

                if (newFromDate[2] == '/' && newFromDate[5] == '/' && newFromDate.length == 10) {
                    val month = newFromDate.substring(0, 2).toIntOrNull()
                    val day = newFromDate.substring(3, 5).toIntOrNull()
                    val year = newFromDate.substring(6).toIntOrNull()
                    isFromDateValid.value = Month.values().any { it.value == month } && (2020..date.year).any { it == year }
                    if (isFromDateValid.value) {
                        val current = LocalDate.of(year!!, month!!, 1).lengthOfMonth()
                        if (day != null) {
                            fromDate.value = LocalDate.of(year, month, if (day > current) current else if (day < 1) 1 else day)
                        }
                    }
                }

                if (newToDate[2] == '/' && newToDate[5] == '/' && newToDate.length == 10) {
                    val month = newToDate.substring(0, 2).toIntOrNull()
                    val day = newToDate.substring(3, 5).toIntOrNull()
                    val year = newToDate.substring(6).toIntOrNull()
                    isToDateValid.value = Month.values().any { it.value == month } && (2020..date.year).any { it == year }
                    if (isToDateValid.value) {
                        val current = LocalDate.of(year!!, month!!, 1).lengthOfMonth()
                        if (day != null) {
                            toDate.value = LocalDate.of(year, month, if (day > current) current else if (day < 1) 1 else day)

                            if (fromDate.value < toDate.value) {
                                from.value = fromDate.value
                                to.value = toDate.value
                            } else {
                                isFromDateValid.value = false
                                isToDateValid.value = false
                            }
                        } else {
                            isFromDateValid.value = false
                            isToDateValid.value = false
                        }
                    }
                }
                if (isToDateValid.value && isFromDateValid.value) {
                    onSubmit.invoke(reportDateFormat.value, from.value, to.value)
                }
            }, enabled = fromDateTextFieldValue.value.length == 10 && toDateTextFieldValue.value.length == 10) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent),
                onClick = onCancel,
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Inventory, contentDescription = null)
        }
    )
}

@Composable
fun ActiveListDialog(
    onCancel: () -> Unit,
    onSubmit: (List<Boolean>) -> Unit
) {
    val pairs = remember {
        mutableStateListOf(true, true)
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Select whether active/inactive items are included")
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                itemsIndexed(pairs.toList()) { index, it ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = it, onCheckedChange = { value -> pairs[index] = value })
                        Text(text = if (index == 0) "Active" else "Inactive")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit.invoke(pairs) },
                enabled = pairs.contains(true)
            ) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent),
                onClick = onCancel,
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
        }
    )
}

@Composable
fun BranchDialog(
    branches: List<Branch>,
    onCancel: () -> Unit,
    onSubmit: (List<Branch>, Boolean) -> Unit
) {
    val pairs = remember {
        mutableStateMapOf(*branches.map { it to false}.toTypedArray())
    }

    val inactive = remember { mutableStateOf(false) }

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
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = inactive.value, onCheckedChange = { value -> inactive.value = value })
                        Text(text = "Include Inactive Items")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit.invoke(pairs.filter { it.value }.keys.toList(), inactive.value) },
                enabled = pairs.values.contains(true)
            ) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent),
                onClick = onCancel,
            ) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Inventory, contentDescription = null)
        }
    )
}