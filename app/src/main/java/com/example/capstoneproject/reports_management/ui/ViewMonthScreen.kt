package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewMonthScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    productViewModel: ProductViewModel,
    contactViewModel: ContactViewModel,
    month: String
) {
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())

    Scaffold(
        topBar = {
            BaseTopAppBar(
                title = month,
                scope = scope,
                scaffoldState = scaffoldState,
                actions = {

                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
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
                                text = it.second.transaction.monthlySales.getOrDefault(key = month, defaultValue = 0).toString(),
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