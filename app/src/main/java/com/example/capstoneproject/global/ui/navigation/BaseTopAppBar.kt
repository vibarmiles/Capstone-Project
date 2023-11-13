package com.example.capstoneproject.global.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BaseTopAppBar(
    title: String,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    actions: (@Composable RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = { Text(text = (title).uppercase()) },
        navigationIcon = { IconButton(onClick = {
            scope.launch { scaffoldState.drawerState.open() }
        })  {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        actions = actions
    )
}