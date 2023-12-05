package com.example.capstoneproject.supplier_management.ui.return_order

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import kotlinx.coroutines.CoroutineScope
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReturnOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    returnOrderViewModel: ReturnOrderViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val returnOrders = returnOrderViewModel.getAll().observeAsState(listOf())
    val firstLaunch = remember { mutableStateOf(true) }
    val state = returnOrderViewModel.result.collectAsState()
    val returnOrdersList = remember(returnOrders.value) {
        returnOrders.value.groupBy {
            val localDate = if (it.date != null) Instant.ofEpochMilli(it.date.time).atZone(ZoneId.systemDefault()).toLocalDate() else LocalDate.now()
            localDate!!
        }
    }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.return_order), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (returnOrderViewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .padding(paddingValues)) {

                LazyColumn {
                    returnOrdersList.toList().sortedByDescending { document -> document.first }.forEach { document ->
                        stickyHeader {
                            Column(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colors.surface)
                                    .fillMaxWidth()
                                    .padding(16.dp)) {
                                Text(
                                    text = DateTimeFormatter.ofLocalizedDate(
                                        FormatStyle.FULL
                                    ).format(document.first),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.secondary
                                )
                            }
                            Divider()
                        }

                        items(
                            items = document.second,
                            key = { it.id }
                        ) {
                            ReturnOrderItem(returnOrder = it, goto = { id -> view.invoke(id) })
                        }
                    }

                    item {
                        if (returnOrderViewModel.returnSize.value == 10) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(4.dp)) {
                                Button(onClick = { returnOrderViewModel.load() }) {
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
                    returnOrderViewModel.resetMessage()
                } else if (state.value.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    returnOrderViewModel.resetMessage()
                }
            }

            LaunchedEffect(key1 = returnOrders.value) {
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
fun ReturnOrderItem(
    returnOrder: ReturnOrder,
    goto: (String) -> Unit
) {
    val time = remember {
        returnOrder.date!!.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
    }
    androidx.compose.material3.ListItem(
        colors = ProjectListItemColors(),
        modifier = Modifier.clickable { goto.invoke(returnOrder.id) },
        headlineContent = {
            Column {
                Text(
                    text = "${returnOrder.products.values.sumOf { it.quantity }} Units",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = time.format(DateTimeFormatter.ofPattern("hh:mm a")))
            }
        },
        supportingContent = {
            Column {
                Text(text = "Reason: ${returnOrder.reason}")
            }
        },
        trailingContent = {
            Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                Text(
                    text = when (returnOrder.status) {
                        Status.WAITING -> "To Return"
                        Status.CANCELLED -> "Cancelled"
                        Status.COMPLETE -> "Returned"
                        Status.PENDING -> "Updating"
                        Status.FAILED -> "Failed"
                    }, fontSize = 12.sp, color = when (returnOrder.status) {
                        Status.WAITING -> Color.Red
                        Status.CANCELLED -> Color.Gray
                        Status.COMPLETE -> Color(ColorUtils.blendARGB(Color.Green.toArgb(), Color.Black.toArgb(), 0.2f))
                        Status.PENDING -> Color.Black
                        Status.FAILED -> Color.Red
                    }, fontWeight = FontWeight.Bold
                )
            }
        }
    )
    Divider()
}