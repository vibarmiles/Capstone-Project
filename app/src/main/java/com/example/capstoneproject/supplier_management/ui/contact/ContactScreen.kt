package com.example.capstoneproject.supplier_management.ui.contact

import android.util.Log
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
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import kotlinx.coroutines.CoroutineScope

@Composable
fun ContactScreen(scope: CoroutineScope, scaffoldState: ScaffoldState, contactViewModel: ContactViewModel, edit: (String, String, String, String) -> Unit, set: (String, String, String) -> Unit, add: () -> Unit) {
    val contacts = contactViewModel.contacts
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pair: Pair<String, String>? = null
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
        ContactScreenContent(paddingValues = paddingValues,contacts = contacts, edit = { edit.invoke(it.first, it.second.name, it.second.contact, it.second.product.toString()) }, set = { set.invoke(it.first, it.second.name, it.second.product.toString()) }) {
            pair = it
            showDeleteDialog = true
        }

        if (showDeleteDialog) {
            ConfirmDeletion(item = pair!!.second, onCancel = { showDeleteDialog = false }) {
                contactViewModel.delete(key = pair!!.first)
                showDeleteDialog = false
            }
        }
    }
}

@Composable
fun ContactScreenContent(paddingValues: PaddingValues, contacts: Map<String, Contact>, edit: (Pair<String, Contact>) -> Unit, set: (Pair<String, Contact>) -> Unit, delete: (Pair<String, String>) -> Unit) {
    LazyColumn(modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            val size = contacts.count()
            Text(modifier = Modifier.padding(16.dp), text = when (size) { 0 -> "There are no entered suppliers"; 1 -> "1 supplier is entered"; else -> "$size suppliers are entered"})
        }

        itemsIndexed(contacts.toList()) {
            _, it ->
            var expanded: Boolean by remember { mutableStateOf(false) }
            ListItem(leadingContent = { Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Filled.ContactPhone, contentDescription = null) }, headlineContent = { Text(text = it.second.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) }, supportingContent = { Text(text = it.second.contact, maxLines = 1, overflow = TextOverflow.Ellipsis) }, trailingContent = {
                IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Contact") }, onClick = { expanded = false; edit.invoke(it) })
                    androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Sell, contentDescription = null) }, text = { Text(text = "Offered Items") }, onClick = { expanded = false; set.invoke(Pair(it.first, it.second)) })
                    androidx.compose.material3.DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Contact") }, onClick = { expanded = false; delete.invoke(Pair(it.first, it.second.name)) })
                }
            })
        }

        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}