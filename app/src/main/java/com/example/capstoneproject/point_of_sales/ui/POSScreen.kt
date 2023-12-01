package com.example.capstoneproject.point_of_sales.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import kotlinx.coroutines.CoroutineScope
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun POSScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    posViewModel: POSViewModel,
    userViewModel: UserViewModel,
    branchViewModel: BranchViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val invoices = posViewModel.getAll().observeAsState(listOf())
    val state = posViewModel.result.collectAsState()
    val firstLaunch = remember { mutableStateOf(true) }
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()
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
                    var currentDate = LocalDate.now().plusDays(1)
                    invoices.value.filter { if (userAccountDetails.value.userLevel != UserLevel.Employee) true else it.branchId == userAccountDetails.value.branchId }.sortedByDescending { document -> document.date }.forEach { invoice ->
                        val localDateTime = if (invoice.date != null) Instant.ofEpochMilli(invoice.date.time).atZone(ZoneId.systemDefault()).toLocalDateTime() else LocalDateTime.now()
                        val date = localDateTime.toLocalDate()
                        val time = localDateTime.toLocalTime()

                        if (currentDate != date) {
                            currentDate = date

                            stickyHeader {
                                Column(modifier = Modifier
                                    .background(color = MaterialTheme.colors.surface)
                                    .fillMaxWidth()
                                    .padding(16.dp)) {
                                    Text(
                                        text = DateTimeFormatter.ofLocalizedDate(
                                            FormatStyle.FULL
                                        ).format(date),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.secondary
                                    )
                                }
                                Divider()
                            }
                        }

                        item {
                            POSItem(time = time, invoice = invoice, branchViewModel = branchViewModel, goto = { view.invoke(it) })
                        }
                    }

                    item {
                        if (posViewModel.returnSize.value == 10) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .padding(4.dp)) {
                                Button(onClick = { posViewModel.load()  }) {
                                    Text(text = "Load More")
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(key1 = state.value) {
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
    time: LocalTime,
    invoice: Invoice,
    branchViewModel: BranchViewModel,
    goto: (String) -> Unit
) {
    Column {
        androidx.compose.material3.ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { goto.invoke(invoice.id) },
            colors = ProjectListItemColors(),
            headlineContent = {
                Column {
                    Text(text = buildAnnotatedString {
                        if (invoice.invoiceType == InvoiceType.SALE) {
                            append("₱${String.format("%.2f", invoice.products.values.sumOf { it.quantity * it.price })}")
                        } else {
                            withStyle(
                                style = SpanStyle(color = MaterialTheme.colors.error,)
                            ) {
                                append("₱${String.format("%.2f", invoice.products.values.sumOf { it.quantity * it.price })}")
                            }
                        }
                    }, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = time.format(DateTimeFormatter.ofPattern("hh:mm a")))
                    Text(text = branchViewModel.getBranch(invoice.branchId)?.name ?: "Unknown Branch")
                }
            }
        )
        Divider()
    }
}