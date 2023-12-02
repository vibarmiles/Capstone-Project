package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun ReportsScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    productViewModel: ProductViewModel,
) {
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

        }
    }
}