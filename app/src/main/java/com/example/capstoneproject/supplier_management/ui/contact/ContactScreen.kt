package com.example.capstoneproject.supplier_management.ui.contact

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ArchiveEntry
import com.example.capstoneproject.global.ui.misc.MakeInactiveDialog
import com.example.capstoneproject.global.ui.misc.ProjectListItemColors
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import kotlinx.coroutines.CoroutineScope

@Composable
fun ContactScreen(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    contactViewModel: ContactViewModel,
    edit: (Contact) -> Unit,
    add: () -> Unit
) {
    val contacts = contactViewModel.getAll().observeAsState(listOf())
    var contact: Contact? = null
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state by contactViewModel.result.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.contact), scope = scope, scaffoldState = scaffoldState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = add) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) {
            paddingValues ->
        if (contactViewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            ContactScreenContent(
                paddingValues = paddingValues,
                contacts = contacts.value.sortedBy { it.name.uppercase() },
                edit = { edit.invoke(it) },
                dialNumber = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0${it.contact}"))) },
                archive = { contact, remove ->
                    contactViewModel.archiveItem(contact = contact, remove = remove)
                }
            ) {
                contact = it
                showDeleteDialog = true
            }
        }

        if (showDeleteDialog) {
            MakeInactiveDialog(item = contact!!.name, onCancel = { showDeleteDialog = false }, function = if (contact!!.active) "Inactive" else "Active") {
                contactViewModel.delete(contact = contact!!)
                showDeleteDialog = false
            }
        }

        LaunchedEffect(key1 = state.result, state.errorMessage) {
            if (!state.result && state.errorMessage != null) {
                scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
                contactViewModel.resetMessage()
            } else if (state.result) {
                scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                contactViewModel.resetMessage()
            }
        }
    }
}

@Composable
fun ContactScreenContent(
    paddingValues: PaddingValues,
    contacts: List<Contact>,
    edit: (Contact) -> Unit,
    dialNumber: (Contact) -> Unit,
    archive: (Contact, Boolean) -> Unit,
    delete: (Contact) -> Unit
) {
    val size = contacts.count()
    Column {
        Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered suppliers"; 1 -> "1 supplier is entered"; else -> "$size suppliers are entered"})
        Divider()
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(
                items = contacts,
                key = { it.id }
            ) {
                var expanded: Boolean by remember { mutableStateOf(false) }
                Column {
                    ListItem(
                        modifier = Modifier.clickable { edit(it) },
                        colors = ProjectListItemColors(),
                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(50.dp),
                                imageVector = Icons.Filled.ContactPhone,
                                contentDescription = null
                            )
                        },
                        headlineContent = {
                            Text(
                                text = it.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                color = if (it.active) MaterialTheme.colors.onSurface else MaterialTheme.colors.error
                            )
                        },
                        supportingContent = {
                            Text(
                                text = it.contact,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingContent = {
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
                                    leadingIcon = { Icon(imageVector = Icons.Default.ContactPhone, contentDescription = null) },
                                    text = { Text(text = "Dial Number") },
                                    onClick = { dialNumber.invoke(it) }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    leadingIcon = { Icon(imageVector = if (it.active) Icons.Outlined.Block else Icons.Default.Add, contentDescription = null) },
                                    text = { Text(text = if (it.active) "Set Inactive" else "Set Active") },
                                    onClick = { expanded = false; delete.invoke(it) }
                                )

                                ArchiveEntry(name = it.name, isActive = it.active) { remove ->
                                    archive.invoke(it, remove)
                                    expanded = false
                                }
                            }
                        }
                    )
                    Divider()
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}