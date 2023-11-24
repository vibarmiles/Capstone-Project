package com.example.capstoneproject.supplier_management.ui.return_order

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import com.example.capstoneproject.supplier_management.ui.FilterByDate
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@Composable
fun ReturnOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    returnOrderViewModel: ReturnOrderViewModel,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val returnOrders by returnOrderViewModel.getAll().observeAsState(listOf())
    var noOfDaysShown by remember { mutableStateOf(0) }
    val days = listOf(1, 3, 7, 30)
    val state = returnOrderViewModel.result.collectAsState()
    val returnOrdersFilteredByDays = remember(returnOrders, noOfDaysShown) {
        mutableStateOf(returnOrders.filter { returnOrders -> LocalDate.parse(returnOrders.date) >= LocalDate.now().minusDays(days[noOfDaysShown].toLong())})
    }

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
                FilterByDate(onClick = { noOfDaysShown = it })

                LazyColumn {
                    itemsIndexed(returnOrdersFilteredByDays.value) {
                            _, it ->
                        ReturnOrderItem(returnOrder = it, goto = { view.invoke(it) })
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
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

            LaunchedEffect(key1 = returnOrders) {
                scaffoldState.snackbarHostState.showSnackbar(message = "Updating", duration = SnackbarDuration.Short)
            }
        }
    }
}

@Composable
fun ReturnOrderItem(
    returnOrder: ReturnOrder,
    goto: (String) -> Unit
) {
    androidx.compose.material3.ListItem(
        colors = ProjectListItemColors(),
        modifier = Modifier.clickable { goto.invoke(returnOrder.id) },
        headlineContent = { Text(text = returnOrder.date) },
        supportingContent = {
            Column {
                Text(text = "Number of Items: ${returnOrder.products.count()}")
                Text(text = "Reason: ${returnOrder.reason}")
            }
        },
        trailingContent = {
            Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                Text(
                    text = "${returnOrder.products.values.sumOf { it.quantity }} Units",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (returnOrder.status) {
                        Status.WAITING -> "To Return"
                        Status.CANCELLED -> "Cancelled"
                        Status.COMPLETE -> "Returned"
                    }, fontSize = 12.sp, color = when (returnOrder.status) {
                        Status.WAITING -> Color.Red
                        Status.CANCELLED -> Color.Gray
                        Status.COMPLETE -> Color.Green
                    }, fontWeight = FontWeight.Bold
                )
            }
        }
    )
    Divider()
}