package com.example.capstoneproject.supplier_management.ui.transfer_order

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrder
import kotlinx.coroutines.CoroutineScope

@Composable
fun TransferOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    transferOrderViewModel: TransferOrderViewModel,
    branchViewModel: BranchViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val transferOrders = transferOrderViewModel.getAll().observeAsState(listOf())
    val firstLaunch = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val state = transferOrderViewModel.result.collectAsState()

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.transfer_order), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (transferOrderViewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .padding(paddingValues)) {
                LazyColumn {
                    itemsIndexed(transferOrders.value) {
                            _, it ->
                        TransferOrderItem(transferOrder = it, branchViewModel = branchViewModel, goto = { view.invoke(it) })
                    }

                    item {
                        if (transferOrderViewModel.returnSize.value == 10) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(4.dp)) {
                                Button(onClick = { transferOrderViewModel.load()  }) {
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
                    transferOrderViewModel.resetMessage()
                } else if (state.value.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    transferOrderViewModel.resetMessage()
                }
            }

            LaunchedEffect(key1 = transferOrders.value) {
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
fun TransferOrderItem(
    transferOrder: TransferOrder,
    branchViewModel: BranchViewModel,
    goto: (String) -> Unit
) {
    Column(
        modifier = Modifier.clickable { goto.invoke(transferOrder.id) }
    ) {
        androidx.compose.material3.ListItem(
            colors = ProjectListItemColors(),
            headlineContent = { Text(text = transferOrder.date.toString()) },
            supportingContent = {
                Column {
                    Text(text = "Number of Items: ${transferOrder.products.count()}")
                }
            },
            trailingContent = {
                Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${transferOrder.products.values.sumOf { it.quantity }} Units",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (transferOrder.status) {
                            Status.WAITING -> "To Transfer"
                            Status.CANCELLED -> "Cancelled"
                            Status.COMPLETE -> "Transferred"
                            Status.PENDING -> "Updating"
                            Status.FAILED -> "Failed"
                        }, fontSize = 12.sp, color = when (transferOrder.status) {
                            Status.WAITING -> Color.Red
                            Status.CANCELLED -> Color.Gray
                            Status.COMPLETE -> Color.Green
                            Status.PENDING -> Color.Black
                            Status.FAILED -> Color.Red
                        }, fontWeight = FontWeight.Bold
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .height(intrinsicSize = IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = branchViewModel.getBranch(transferOrder.oldBranchId)?.name ?: "Unknown Branch")
            Box(modifier = Modifier
                .height(1.dp)
                .padding(horizontal = 8.dp)
                .background(color = Color.Black)
                .weight(1f))
            Text(text = branchViewModel.getBranch(transferOrder.destinationBranchId)?.name ?: "Unknown Branch")
        }

        Divider()
    }
}