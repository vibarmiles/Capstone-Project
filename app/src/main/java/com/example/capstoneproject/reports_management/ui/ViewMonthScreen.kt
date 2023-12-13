package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewMonthScreen(
    productViewModel: ProductViewModel,
    contactViewModel: ContactViewModel,
    userAccountDetails: UserAccountDetails,
    month: String,
    year: Int,
    dismissRequest: () -> Unit
) {
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val date = Instant.ofEpochMilli(userAccountDetails.loginDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val getCurrent = remember { mutableStateOf(date.year == year && date.month.name == month) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = month.uppercase()) },
                navigationIcon = {
                    IconButton(onClick = dismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            stickyHeader {
                Column {
                    ListItem(
                        colors = ProjectListItemColors(),
                        headlineContent = {
                            Text(
                                text = "Products",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingContent = {
                            Text(text = "Units Sold")
                        }
                    )
                    Divider()
                }
            }

            items(
                items = products.toList().sortedBy { it.second.productName },
                key = {
                    it.first
                }
            ) {
                Column {
                    ListItem(
                        colors = ProjectListItemColors(),
                        headlineContent = {
                            Text(text = it.second.productName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text(text = suppliers.value.firstOrNull { supplier -> supplier.id == it.second.supplier }?.name ?: "Unknown Supplier")
                        },
                        trailingContent = {
                            Text(
                                text = if (getCurrent.value) it.second.transaction.soldThisMonth.toString() else it.second.transaction.monthlySales.getOrDefault(key = year.toString(), mapOf()).getOrDefault(key = month, defaultValue = 0).toString(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }
}