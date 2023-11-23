package com.example.capstoneproject.point_of_sales.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.supplier_management.ui.FilterByDate
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@Composable
fun POSScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    posViewModel: POSViewModel,
    branchViewModel: BranchViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val invoices by posViewModel.getAll().observeAsState(listOf())
    var noOfDaysShown by remember { mutableStateOf(0) }
    val days = listOf(1, 3, 7, 30)
    val state by posViewModel.result.collectAsState()
    val invoicesFilteredByDays = remember(invoices, noOfDaysShown) {
        mutableStateOf(invoices.filter { invoices -> LocalDate.parse(invoices.date) >= LocalDate.now().minusDays(days[noOfDaysShown].toLong())})
    }

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.pos), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { paddingValues ->
        if (posViewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .padding(paddingValues)) {
                FilterByDate(onClick = { noOfDaysShown = it })

                LazyColumn {
                    itemsIndexed(invoicesFilteredByDays.value) {_, it->
                        POSItem(invoice = it, branchViewModel = branchViewModel, goto = { view.invoke(it) })
                        Divider()
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }

            LaunchedEffect(key1 = state.result, state.errorMessage) {
                if (!state.result && state.errorMessage != null) {
                    scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
                    posViewModel.resetMessage()
                } else if (state.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    posViewModel.resetMessage()
                }
            }
        }
    }
}

@Composable
fun POSItem(
    invoice: Invoice,
    branchViewModel: BranchViewModel,
    goto: (String) -> Unit
) {
    androidx.compose.material3.ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { goto.invoke(invoice.id) },
        colors = ProjectListItemColors(),
        headlineContent = {
            Column {
                Text(text = branchViewModel.getBranch(invoice.branchId)?.name ?: "Unknown Branch")
                Text(text = invoice.date)
            }
        },
        supportingContent = {
            Column {
                Text(text = "Number of Items: ${invoice.products.count()}")
            }
        },
        trailingContent = {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    buildAnnotatedString {
                        if (invoice.invoiceType == InvoiceType.SALE) {
                            withStyle(style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                                append("${invoice.products.values.sumOf { it.quantity * it.price }} PHP")
                            }
                        } else {
                            withStyle(style = SpanStyle(color = MaterialTheme.colors.error, fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                                append("(${invoice.products.values.sumOf { it.quantity * it.price }} PHP)")
                            }
                        }
                    }
                )
            }
        },
    )
}