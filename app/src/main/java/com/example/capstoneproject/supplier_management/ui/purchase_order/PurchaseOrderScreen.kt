package com.example.capstoneproject.supplier_management.ui.purchase_order

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
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
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.Status
import kotlinx.coroutines.CoroutineScope
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val purchaseOrders = purchaseOrderViewModel.getAll().observeAsState(listOf())
    val firstLaunch = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val state = purchaseOrderViewModel.result.collectAsState()

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.purchase_order), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (purchaseOrderViewModel.isLoading.value) {
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
                    purchaseOrders.value.sortedByDescending { document -> document.date }.forEach { po ->
                        val localDateTime = if (po.date != null) Instant.ofEpochMilli(po.date.time).atZone(ZoneId.systemDefault()).toLocalDateTime() else LocalDateTime.now()
                        val date = localDateTime.toLocalDate()
                        val time = localDateTime.toLocalTime()

                        if (currentDate != date) {
                            currentDate = date

                            stickyHeader {
                                Column(modifier = Modifier
                                    .background(color = MaterialTheme.colors.surface)
                                    .fillMaxWidth()
                                    .padding(16.dp)) {
                                    Text(text = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(date), fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary)
                                }
                                Divider()
                            }
                        }

                        item {
                            PurchaseOrderItem(time = time, purchaseOrder = po, goto = { view.invoke(it) })
                        }
                    }

                    item {
                        if (purchaseOrderViewModel.returnSize.value == 10) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .padding(4.dp)) {
                                Button(onClick = { purchaseOrderViewModel.load() }) {
                                    androidx.compose.material.Text(text = "Load More")
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(key1 = state.value) {
                Log.e("PURCHASE ORDER", state.value.toString())
                if (!state.value.result && state.value.errorMessage != null) {
                    scaffoldState.snackbarHostState.showSnackbar(message = state.value.errorMessage!!, duration = SnackbarDuration.Short)
                    purchaseOrderViewModel.resetMessage()
                } else if (state.value.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    purchaseOrderViewModel.resetMessage()
                }
            }


            LaunchedEffect(key1 = purchaseOrders.value) {
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
fun PurchaseOrderItem(
    time: LocalTime,
    purchaseOrder: PurchaseOrder,
    goto: (String) -> Unit
) {
    androidx.compose.material3.ListItem(
        colors = ProjectListItemColors(),
        modifier = Modifier.clickable { goto.invoke(purchaseOrder.id) },
        headlineContent = {
            Column {
                Text(text = "₱${String.format("%.2f", purchaseOrder.products.values.sumOf { (it.price * it.quantity) })}", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = time.format(DateTimeFormatter.ofPattern("hh:mm a")))
            }
        },
        trailingContent = {
            Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                Text(text = when (purchaseOrder.status) {
                    Status.WAITING -> "To Receive"
                    Status.CANCELLED -> "Cancelled"
                    Status.COMPLETE -> "Delivered"
                    Status.PENDING -> "Updating"
                    Status.FAILED -> "Failed"
                }, fontSize = 12.sp, color = when (purchaseOrder.status) {
                    Status.WAITING -> Color.Red
                    Status.CANCELLED -> Color.Gray
                    Status.COMPLETE -> Color(ColorUtils.blendARGB(Color.Green.toArgb(), Color.Black.toArgb(), 0.2f))
                    Status.PENDING -> Color.Black
                    Status.FAILED -> Color.Red
                }, fontWeight = FontWeight.Bold)
            }
        }
    )
    Divider()
}