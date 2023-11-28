package com.example.capstoneproject.supplier_management.ui.contact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
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
            ContactScreenContent(paddingValues = paddingValues, contacts = contacts.value.sortedBy { it.name.uppercase() }, edit = { edit.invoke(it) }) {
                contact = it
                showDeleteDialog = true
            }
        }

        if (showDeleteDialog) {
            MakeInactiveDialog(item = contact!!.name, onCancel = { showDeleteDialog = false }) {
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
    delete: (Contact) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            val size = contacts.count()
            Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered suppliers"; 1 -> "1 supplier is entered"; else -> "$size suppliers are entered"})
        }

        itemsIndexed(contacts.toList()) {
            _, it ->
            var expanded: Boolean by remember { mutableStateOf(false) }
            ListItem(colors = ProjectListItemColors(), leadingContent = { Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Filled.ContactPhone, contentDescription = null) }, headlineContent = { Text(text = it.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = it.contact, maxLines = 1, overflow = TextOverflow.Ellipsis) }, trailingContent = {
                IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Contact") }, onClick = { expanded = false; edit.invoke(it) })
                    androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Contact") }, onClick = { expanded = false; delete.invoke(it) })
                }
            })
        }

        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}