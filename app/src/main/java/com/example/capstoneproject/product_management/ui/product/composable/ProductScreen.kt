package com.example.capstoneproject.product_management.ui.product.composable

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.composable.BaseTopAppBar
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProductScreen(scope: CoroutineScope, scaffoldState: ScaffoldState) {
    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.product), scope = scope, scaffoldState = scaffoldState) {
                IconButton(onClick = {  }) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {  }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
        it
    }
}