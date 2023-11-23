package com.example.capstoneproject.user_management.ui.users

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.user_management.data.firebase.User
import com.example.capstoneproject.user_management.data.firebase.UserLevel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserForm(
    userViewModel: UserViewModel,
    decision: String,
    userId: String? = null,
    back: () -> Unit
) {
    val user = userViewModel.getUserDetails(userId = userId) ?: User()
    var expandedUsers by remember { mutableStateOf(false) }
    var userLevel by remember { mutableStateOf(user.userLevel) }
    var firstName by remember { mutableStateOf(user.firstName) }
    var isFirstNameValid by remember { mutableStateOf(true) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var isLastNameValid by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf(user.email) }
    var isEmailValid by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = ("$decision User").uppercase())
                },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
            paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ExposedDropdownMenuBox(expanded = expandedUsers, onExpandedChange = { expandedUsers = !expandedUsers }) {
                OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUsers) }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = userLevel.name, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.user_level)) })

                DropdownMenu(modifier = Modifier
                    .exposedDropdownSize()
                    .fillMaxWidth(), expanded = expandedUsers, onDismissRequest = { expandedUsers = false }) {
                    enumValues<UserLevel>().forEach {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(text = it.name) }, onClick = { userLevel = it; expandedUsers = false })
                    }
                }
            }
            OutlinedTextField(value = firstName, colors = GlobalTextFieldColors(), onValueChange = { firstName = it.filter { value -> value.isLetter() || value.isWhitespace() } }, label = { Text(text = "First Name") }, placeholder = { Text(text = "Enter First Name") }, isError = !isFirstNameValid, trailingIcon = { if (!isFirstNameValid) Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color.Red
            )}, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, colors = GlobalTextFieldColors(), onValueChange = { lastName = it.filter { value -> value.isLetter() || value.isWhitespace() } }, label = { Text(text = "Last Name") }, placeholder = { Text(text = "Enter Last Name") }, isError = !isLastNameValid, trailingIcon = { if (!isLastNameValid) Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color.Red
            )}, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, colors = GlobalTextFieldColors(), onValueChange = { email = it }, label = { Text(text = "Email") }, placeholder = { Text(text = "Enter Email") }, isError = !isEmailValid, trailingIcon = { if (!isEmailValid) Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color.Red
            )}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }, modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FormButtons(cancel = back) {
                    isFirstNameValid = firstName.isNotBlank()
                    isLastNameValid = lastName.isNotBlank()
                    isEmailValid = email.let { it.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(it).matches() }

                    if (isFirstNameValid && isLastNameValid && isEmailValid) {
                        userViewModel.insert(id = userId, user = user.copy(lastName = lastName, firstName = firstName, email = email, userLevel = userLevel))
                        back.invoke()
                    }
                }
            }
        }
    }
}