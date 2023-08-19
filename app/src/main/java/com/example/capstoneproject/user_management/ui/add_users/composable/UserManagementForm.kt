package com.example.capstoneproject.user_management.ui.add_users.composable

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.R
import com.example.capstoneproject.user_management.data.Room.User
import com.example.capstoneproject.user_management.ui.viewmodel.UserViewModel
import com.example.capstoneproject.user_management.ui.viewmodel.UserViewModelFactory

@Composable
fun UserManagementForm(userId: Int = 0, cancel: () -> Unit) {
    val viewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current.applicationContext as Application))
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text(text = "First Name") }, placeholder = { Text(text = "Enter First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text(text = "Last Name") }, placeholder = { Text(text = "Enter Last Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(text = "Email") }, placeholder = { Text(text = "Enter Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(text = "Password") }, placeholder = { Text(text = "Enter Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text(text = "Confirm Password") }, placeholder = { Text(text = "Confirm Password") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = cancel, border = BorderStroke(1.dp, Color.Black), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black), modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.cancel_button), modifier = Modifier.padding(4.dp))
            }
            Button(onClick = {
                if (firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword) {
                    val user: User = User(lastName = lastName, firstName = firstName, email = email, password = password, salt = "salt")
                    viewModel.insert(user)
                }
            }, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.submit_button), modifier = Modifier.padding(4.dp))
            }
        }
    }
}