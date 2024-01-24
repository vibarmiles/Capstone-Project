package com.example.capstoneproject.supplier_management.ui.transfer_order

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrder
import com.example.capstoneproject.ui.theme.pending
import com.example.capstoneproject.ui.theme.success
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import kotlinx.coroutines.CoroutineScope
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransferOrderScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    transferOrderViewModel: TransferOrderViewModel,
    branchViewModel: BranchViewModel,
    userAccountDetails: UserAccountDetails,
    add: () -> Unit,
    view: (String) -> Unit
) {
    val transferOrders = transferOrderViewModel.getAll().observeAsState(listOf())
    val transferOrdersList = remember(transferOrders.value) {
        transferOrders.value.filter { if (userAccountDetails.userLevel != UserLevel.Owner) it.destinationBranchId == userAccountDetails.branchId else true }.groupBy {
            val localDate = if (it.date != null) Instant.ofEpochMilli(it.date.time).atZone(ZoneId.systemDefault()).toLocalDate() else LocalDate.now()
            localDate!!
        }
    }
    val firstLaunch = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val state = transferOrderViewModel.result.collectAsState()

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.transfer_order), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            if (userAccountDetails.userLevel == UserLevel.Owner) {
                FloatingActionButton(onClick = add) {
                    Icon(Icons.Filled.Add, null)
                }
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
                    transferOrdersList.toList().sortedByDescending { document -> document.first }.forEach { document ->
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
                            items = document.second.sortedByDescending { it.date },
                            key = { it.id }
                        ) {
                            TransferOrderItem(
                                transferOrder = it,
                                branchViewModel = branchViewModel,
                                goto = { id -> view.invoke(id) })
                        }
                    }

                    item {
                        if (transferOrderViewModel.returnSize.value == 10) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .padding(4.dp)) {
                                Button(onClick = { transferOrderViewModel.load() }) {
                                    Text(text = "Load More")
                                }
                            }
                        }
                    }
                }
            }
            
            LaunchedEffect(key1 = state.value.result, state.value.errorMessage) {
                if (!state.value.result && state.value.errorMessage != null) {
                    Toast.makeText(context, state.value.errorMessage!!, Toast.LENGTH_SHORT).show()
                    transferOrderViewModel.resetMessage()
                } else if (state.value.result) {
                    Toast.makeText(context, "Successfully Done!", Toast.LENGTH_SHORT).show()
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
    val time = remember(transferOrder) {
        transferOrder.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
    }
    Column(
        modifier = Modifier.clickable { goto.invoke(transferOrder.id) }
    ) {
        androidx.compose.material3.ListItem(
            colors = ProjectListItemColors(),
            headlineContent = {
                Column {
                    Text(
                        text = "${transferOrder.products.values.sumOf { it.quantity }} Units",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = (time ?: LocalTime.now()).format(DateTimeFormatter.ofPattern("hh:mm a")))
                }
            },
            supportingContent = {
                Column {
                    Text(text = "Number of Items: ${transferOrder.products.count()}")
                }
            },
            trailingContent = {
                Column(modifier = Modifier.height(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (transferOrder.status) {
                                    Status.WAITING -> pending
                                    Status.CANCELLED -> Color.Gray
                                    Status.COMPLETE -> success
                                    Status.PENDING -> Color.Black
                                    Status.FAILED -> Color.Red
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = when (transferOrder.status) {
                                Status.WAITING -> "To Transfer"
                                Status.CANCELLED -> "Cancelled"
                                Status.COMPLETE -> "Transferred"
                                Status.PENDING -> "Updating"
                                Status.FAILED -> "Failed"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transferOrder.status == Status.WAITING) Color.Black else Color.White
                        )
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .background(color = ListItemDefaults.containerColor)
                .height(intrinsicSize = IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = branchViewModel.getBranch(transferOrder.oldBranchId)?.name ?: "Unknown Branch", modifier = Modifier.width(IntrinsicSize.Min))
            Box(modifier = Modifier
                .height(1.dp)
                .padding(horizontal = 8.dp)
                .background(color = Color.Black)
                .weight(1f))
            Text(text = branchViewModel.getBranch(transferOrder.destinationBranchId)?.name ?: "Unknown Branch", modifier = Modifier.width(IntrinsicSize.Min))
        }

        Divider()
    }
}