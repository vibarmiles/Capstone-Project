package com.example.capstoneproject.supplier_management.ui.purchase_order

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.Status
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

/*
* TODO(
*   1.  Clicking the purchase order displays its contents
*   2.  Editing the purchase order's status from WAITING to CANCELLED or DONE
*   3.  Purchased Products should be removed from the product red mark.
* )
*/

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
    val days = listOf(1, 3, 7, 30)
    val state by purchaseOrderViewModel.result.collectAsState()
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(paddingValues).fillMaxSize()) {
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

            LaunchedEffect(key1 = state.result, state.errorMessage) {
                if (!state.result && state.errorMessage != null) {
                    scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
                    purchaseOrderViewModel.resetMessage()
                } else if (state.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    purchaseOrderViewModel.resetMessage()
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
                    Status.DONE -> "Delivered"
                }, fontSize = 12.sp, color = when (purchaseOrder.status) {
                    Status.WAITING -> Color.Red
                    Status.CANCELLED -> Color.Gray
                    Status.DONE -> Color.Green
                }, fontWeight = FontWeight.Bold)
            }
        }
    )
    Divider()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterByDate(
    onClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp), expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        var textFieldValue by remember { mutableStateOf("Today") }
        val dropdownMenuItems = listOf("Today", "Last 3 days", "Last 7 days", "Last 30 days")
        OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = textFieldValue, readOnly = true, onValueChange = {  })
        DropdownMenu(modifier = Modifier
            .exposedDropdownSize()
            .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
            dropdownMenuItems.forEachIndexed {
                    index, s ->
                androidx.compose.material3.DropdownMenuItem(text = { androidx.compose.material.Text(text = s) }, onClick = { onClick.invoke(index); textFieldValue = s; expanded = false })
            }
        }
    }
}