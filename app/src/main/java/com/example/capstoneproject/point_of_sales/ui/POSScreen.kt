package com.example.capstoneproject.point_of_sales.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.CoroutineScope

@Composable
fun POSScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    posViewModel: POSViewModel,
    branchViewModel: BranchViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val invoices = posViewModel.getAll().observeAsState(listOf())
    val state = posViewModel.result.collectAsState()
    val firstLaunch = remember { mutableStateOf(true) }
    val context = LocalContext.current

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
                LazyColumn {
                    itemsIndexed(invoices.value) {_, it->
                        POSItem(invoice = it, branchViewModel = branchViewModel, goto = { view.invoke(it) })
                        Divider()
                    }

                    item {
                        if (posViewModel.returnSize.value == 3) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(4.dp)) {
                                Button(onClick = { posViewModel.load()  }) {
                                    Text(text = "Load More")
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(key1 = state.value.result, state.value.errorMessage) {
                if (!state.value.result && state.value.errorMessage != null) {
                    scaffoldState.snackbarHostState.showSnackbar(message = state.value.errorMessage!!, duration = SnackbarDuration.Short)
                    posViewModel.resetMessage()
                } else if (state.value.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    posViewModel.resetMessage()
                }
            }

            LaunchedEffect(key1 = invoices.value) {
                if (!firstLaunch.value) {
                    Toast.makeText(context, "Updating!", Toast.LENGTH_SHORT).show()
                } else {
                    firstLaunch.value = false
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
                Text(text = invoice.date.toString())
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