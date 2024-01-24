package com.example.capstoneproject.user_management.ui.users

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Block
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ArchiveEntry
import com.example.capstoneproject.global.ui.misc.MakeInactiveDialog
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.ui.theme.Purple500
import com.example.capstoneproject.user_management.data.firebase.User
import com.example.capstoneproject.user_management.data.firebase.UserLevel
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
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val size = users.filterNot { it.value.userLevel == if (userAccountDetails.value.userLevel == UserLevel.Admin) UserLevel.Cashier else UserLevel.Admin }.size
    val listOfUsers = remember(userViewModel.update.value) {
        derivedStateOf {
            users.toList().filterNot { it.second.userLevel == if (userAccountDetails.value.userLevel == UserLevel.Admin) UserLevel.Cashier else UserLevel.Admin }.sortedWith(comparator = compareBy<Pair<String, User>> { it.second.userLevel }.thenBy { it.second.lastName.uppercase() })
        }
    }
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
            Column {
                Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered users"; 1 -> "1 user is entered"; else -> "$size users are entered"})
                Divider()
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(
                        items = listOfUsers.value,
                        key = { it.first }
                    ) {
                        Column {
                            UserListItem(
                                user = it.second,
                                isUser = it.first == userAccountDetails.value.id,
                                edit = { edit.invoke(it.first) },
                                archive = { remove ->
                                    userViewModel.archiveItem(it.first, remove, it.second)
                                }
                            ) {
                                user = it
                                showDeleteDialog = true
                            }
                            Divider()
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                MakeInactiveDialog(item = user.second.email, onCancel = { showDeleteDialog = false }, function = if (user.second.active) "Inactive" else "Active") {
                    userViewModel.delete(user.first, user.second)
                    showDeleteDialog = false
                }
            }

            LaunchedEffect(key1 = state.result, state.errorMessage) {
                if (!state.result && state.errorMessage != null) {
                    Toast.makeText(context, state.errorMessage!!, Toast.LENGTH_SHORT).show()
                    userViewModel.resetMessage()
                } else if (state.result) {
                    Toast.makeText(context, "Successfully Done!", Toast.LENGTH_SHORT).show()
                    userViewModel.resetMessage()
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    isUser: Boolean,
    edit: () -> Unit,
    archive: (Boolean) -> Unit,
    delete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.ListItem(
        modifier = Modifier.clickable { edit.invoke() },
        colors = ProjectListItemColors(),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Purple500,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) { Text(
                text = user.email.first().uppercase(),
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            }
        },
        headlineContent = { Text(
            text = "${user.lastName}, ${user.firstName}",
            color = if (user.active) MaterialTheme.colors.onSurface else MaterialTheme.colors.error
        ) },
        supportingContent = { Text(
            text = user.email,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        ) },
        trailingContent = {
            if (!isUser) {
                IconButton(
                    onClick = { expanded = !expanded },
                    content = {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        leadingIcon = {
                            Icon(imageVector = if (user.active) Icons.Outlined.Block else Icons.Default.Add, contentDescription = null)
                        },
                        text = { Text(text = if (user.active) "Set Inactive" else "Set Active") },
                        onClick = { expanded = false; delete.invoke() }
                    )
                    ArchiveEntry(name = "${user.lastName}, ${user.firstName}", isActive = user.active) {
                        archive.invoke(it)
                        expanded = false
                    }
                }
            }
        }
    )
}