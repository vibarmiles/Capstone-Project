package com.example.capstoneproject.supplier_management.ui.purchase_order

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.ui.FilterByDate
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@Composable
fun PurchaseOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val purchaseOrders by purchaseOrderViewModel.getAll().observeAsState(listOf())
    var noOfDaysShown by remember { mutableStateOf(0) }
    val firstLaunch = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val days = listOf(1, 3, 7, 30)
    val state = purchaseOrderViewModel.result.collectAsState()
    val purchaseOrdersFilteredByDays = remember(purchaseOrders, noOfDaysShown) {
        mutableStateOf(purchaseOrders.filter { purchaseOrder -> LocalDate.parse(purchaseOrder.date) >= LocalDate.now().minusDays(days[noOfDaysShown].toLong())})
    }

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
                FilterByDate(onClick = { noOfDaysShown = it })

                LazyColumn {
                    itemsIndexed(purchaseOrdersFilteredByDays.value) {
                            _, it ->
                        PurchaseOrderItem(purchaseOrder = it, goto = { view.invoke(it) })
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
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


            LaunchedEffect(key1 = purchaseOrders) {
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
    purchaseOrder: PurchaseOrder,
    goto: (String) -> Unit
) {
    androidx.compose.material3.ListItem(
        colors = ProjectListItemColors(),
        modifier = Modifier.clickable { goto.invoke(purchaseOrder.id) },
        headlineContent = { Text(text = purchaseOrder.date) },
        supportingContent = { Text(text = "Number of Items: ${purchaseOrder.products.count()}") },
        trailingContent = { 
            Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                Text(text = "${purchaseOrder.products.values.sumOf { (it.price * it.quantity) }} PHP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = when (purchaseOrder.status) {
                    Status.WAITING -> "To Receive"
                    Status.CANCELLED -> "Cancelled"
                    Status.COMPLETE -> "Delivered"
                    Status.PENDING -> "Updating"
                    Status.FAILED -> "Failed"
                }, fontSize = 12.sp, color = when (purchaseOrder.status) {
                    Status.WAITING -> Color.Red
                    Status.CANCELLED -> Color.Gray
                    Status.COMPLETE -> Color.Green
                    Status.PENDING -> Color.Black
                    Status.FAILED -> Color.Red
                }, fontWeight = FontWeight.Bold)
            }
        }
    )
    Divider()
}