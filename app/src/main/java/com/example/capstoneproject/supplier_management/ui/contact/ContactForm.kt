package com.example.capstoneproject.supplier_management.ui.contact

import android.util.Log
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact

@Composable
fun ContactFormScreen(function: String, contactViewModel: ContactViewModel, contact: Contact? = null, back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "$function Contact") }, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
        paddingValues ->
        var name by remember { mutableStateOf(contact?.name ?: "") }
        var contact by remember { mutableStateOf(contact?.contact ?: "") }
        var isContactValid by remember { mutableStateOf(true) }
        var isNameValid by remember { mutableStateOf(true) }

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(state = rememberScrollState())) {
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = name, onValueChange = { value -> name = value }, placeholder = { Text(text = "Enter Contact's Name") }, label = { Text(text = "Contact's Name") }, isError = !isNameValid, trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) })
            OutlinedTextField(leadingIcon = { Icon(imageVector = Icons.Filled.Phone, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), value = contact, onValueChange = { value -> contact = value }, placeholder = { Text(text = "Enter Contact's Number") }, label = { Text(text = "Contact's Number") }, isError = !isContactValid, trailingIcon = { if (!isContactValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isContactValid = contact.isNotBlank()
                if (isContactValid && isNameValid) {
                    contactViewModel.insert(contact = Contact(name = name, contact = contact))
                    back.invoke()
                }
            }
        }
    }
}