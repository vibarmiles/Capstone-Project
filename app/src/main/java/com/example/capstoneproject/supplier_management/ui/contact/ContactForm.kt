package com.example.capstoneproject.supplier_management.ui.contact

import android.util.Patterns
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@Composable
fun ContactFormScreen(
    function: String,
    contactViewModel: ContactViewModel,
    id: String? = null,
    userViewModel: UserViewModel,
    back: () -> Unit
) {
    val oldContact = contactViewModel.getContact(id) ?: Contact()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = ("$function Contact").uppercase()) }, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
            paddingValues ->
        var name by remember { mutableStateOf(oldContact.name) }
        var contact by remember { mutableStateOf(oldContact.contact) }
        var isContactValid by remember { mutableStateOf(true) }
        var isNameValid by remember { mutableStateOf(true) }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())) {
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), colors = GlobalTextFieldColors(), value = name, onValueChange = { value -> name = value }, placeholder = { Text(text = "Enter Contact's Name") }, label = { Text(text = buildAnnotatedString { append("Contact's Name"); withStyle( style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") } }) }, isError = !isNameValid, trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) })
            OutlinedTextField(leadingIcon = { Text(text = "+63") }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = contact, onValueChange = { value -> if (value.length <= 10) contact = value.filter { it.isDigit() } }, placeholder = { Text(text = "Enter Contact's Phone Number") }, label = { Text(text = buildAnnotatedString { append("Contact's Number"); withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") } }) }, isError = !isContactValid, trailingIcon = { if (!isContactValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isContactValid = contact.let { it.isNotBlank() && Patterns.PHONE.matcher(it).matches() && it.length == 10 }
                if (isContactValid && isNameValid) {
                    contactViewModel.insert(contact = oldContact.copy(name = name, contact = contact))
                    userViewModel.log(event = "${function.lowercase()}_supplier")
                    back.invoke()
                }
            }
        }
    }
}