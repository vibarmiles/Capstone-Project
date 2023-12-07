package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    productViewModel: ProductViewModel,
    contactViewModel: ContactViewModel,
    userAccountDetails: UserAccountDetails
) {
    val pagerState = rememberPagerState(0)
    val date = Instant.ofEpochMilli(userAccountDetails.loginDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val products = productViewModel.getAll()
    val suppliers = contactViewModel.getAll().observeAsState(listOf())
    val productsWithInventoryTurnoverRatio = products.values
        .map {
            val averageStock = (it.transaction.openingStock + it.transaction.closingStock).toDouble() / 2
            val sales = it.transaction.monthlySales.values.sum().toDouble()
            val turnoverRatio = if (averageStock != 0.0) sales / averageStock else 0.0
            turnoverRatio to it
        }
        .sortedWith(compareByDescending<Pair<Double, Product>> { it.first }.thenBy { it.second.productName })
    
    Scaffold(
        topBar = {
            BaseTopAppBar(
                title = "Reports",
                scope = scope,
                scaffoldState = scaffoldState,
                actions = {

                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                Tab(selected = pagerState.currentPage == 0, onClick = { scope.launch { pagerState.animateScrollToPage(0) } }) {
                    Text(text = "FSN Analysis")
                }
                Tab(selected = pagerState.currentPage == 1, onClick = { scope.launch { pagerState.animateScrollToPage(1) } }) {
                    Text(text = "Turnover Ratio")
                }
                Tab(selected = pagerState.currentPage == 2, onClick = { scope.launch { pagerState.animateScrollToPage(2) } }) {
                    Text(text = "Monthly Sales")
                }
            }

            HorizontalPager(pageCount = 3, state = pagerState) {
                when (it) {
                    0 -> FSNAnalysis(products = productsWithInventoryTurnoverRatio, suppliers = suppliers.value)
                    1 -> TurnoverRatio(products = productsWithInventoryTurnoverRatio, suppliers = suppliers.value)
                    2 -> MonthlySales(date = date, products = products.values.toList(), showData = {  })
                }
            }
        }
    }
}