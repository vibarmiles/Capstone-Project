package com.example.capstoneproject.supplier_management.ui.transfer_order

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.capstoneproject.supplier_management.ui.FilterByDate
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@Composable
fun TransferOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    transferOrderViewModel: TransferOrderViewModel,
    branchViewModel: BranchViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val transferOrders by transferOrderViewModel.getAll().observeAsState(listOf())
    var noOfDaysShown by remember { mutableStateOf(0) }
    val days = listOf(1, 3, 7, 30)
    val state = transferOrderViewModel.result.collectAsState()
    val transferOrdersFilteredByDays = remember(transferOrders, noOfDaysShown) {
        mutableStateOf(transferOrders.filter { returnOrders -> LocalDate.parse(returnOrders.date) >= LocalDate.now().minusDays(days[noOfDaysShown].toLong())})
    }

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
                FilterByDate(onClick = { noOfDaysShown = it })

                LazyColumn {
                    itemsIndexed(transferOrdersFilteredByDays.value) {
                            _, it ->
                        TransferOrderItem(transferOrder = it, branchViewModel = branchViewModel, goto = { view.invoke(it) })
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
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

            LaunchedEffect(key1 = transferOrders) {
                scaffoldState.snackbarHostState.showSnackbar(message = "Updating", duration = SnackbarDuration.Short)
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
            headlineContent = { Text(text = transferOrder.date) },
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
                        }, fontSize = 12.sp, color = when (transferOrder.status) {
                            Status.WAITING -> Color.Red
                            Status.CANCELLED -> Color.Gray
                            Status.COMPLETE -> Color.Green
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