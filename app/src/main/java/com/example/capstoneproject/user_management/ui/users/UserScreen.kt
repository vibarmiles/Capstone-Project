package com.example.capstoneproject.user_management.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.ui.theme.Purple500
import com.example.capstoneproject.user_management.data.firebase.User
import kotlinx.coroutines.CoroutineScope

@Composable
fun UserScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    userViewModel: UserViewModel,
    add: () -> Unit,
    edit: (String) -> Unit
) {
    val users = userViewModel.getAll()
    val state by userViewModel.result.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    lateinit var user: Pair<String, User>

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.user), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
        FloatingActionButton(onClick = add) {
            Icon(Icons.Filled.Add, null)
        }
    }) {
            paddingValues ->
        if (userViewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            val listOfUsers = remember(userViewModel.update) {
                derivedStateOf {
                    users.toList().sortedBy { pair -> pair.second.userLevel }
                }
            }

            LazyColumn(modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    val size = users.size
                    Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered users"; 1 -> "1 user is entered"; else -> "$size users are entered"})
                }

                itemsIndexed(listOfUsers.value) {
                        _, it ->
                    UserListItem(user = it.second, edit = { edit.invoke(it.first) }) {
                        user = it
                        showDeleteDialog = true
                    }
                }
            }

            if (showDeleteDialog) {
                ConfirmDeletion(item = user.second.email, onCancel = { showDeleteDialog = false }) {
                    userViewModel.delete(user.first)
                    showDeleteDialog = false
                }
            }

            LaunchedEffect(key1 = state.result, state.errorMessage) {
                if (!state.result && state.errorMessage != null) {
                    scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
                    userViewModel.resetMessage()
                } else if (state.result) {
                    scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                    userViewModel.resetMessage()
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    edit: () -> Unit,
    delete: () -> Unit
) {
    androidx.compose.material3.ListItem(colors = ProjectListItemColors(), leadingContent = {
        Box(modifier = Modifier
            .size(50.dp)
            .background(color = Purple500, shape = CircleShape), contentAlignment = Alignment.Center) { Text(text = user.email.substring(startIndex = 0, endIndex = 1), fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center)
        }
    }, headlineContent = { Text(text = "${user.lastName}, ${user.firstName}") }, supportingContent = { Text(text = user.email) }, trailingContent = {
        Row {
            IconButton(onClick = edit) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = delete) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    })
}