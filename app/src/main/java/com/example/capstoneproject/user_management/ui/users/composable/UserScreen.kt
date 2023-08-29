package com.example.capstoneproject.user_management.ui.users.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import kotlinx.coroutines.CoroutineScope

@Composable
fun UserScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, add: () -> Unit, edit: (String) -> Unit) {
    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.user), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
        FloatingActionButton(onClick = add) {
            Icon(Icons.Filled.Add, null)
        }
    }) {
        it
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        }
    }
}

@Composable
fun UserListItem(name: String = "User's Name", email: String = "Cashier@email.com", edit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.size(50.dp)) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)) {
                Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = email, fontSize = 16.sp)
            }
            IconButton(onClick = edit) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
        }
    }
    Divider()
}