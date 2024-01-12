package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R

@Composable
fun FirstLoginDialog(
    changePassword: Boolean,
    onSubmit: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(true) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {  },
        title = {
            Text(text = "Change Password")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (changePassword) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text(text = "Old Password") },
                        isError = !isValid,
                        trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                    )
                }

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "New Password") },
                    isError = !isValid,
                    trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                )

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Confirm Password") },
                    isError = !isValid,
                    trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (password == confirmPassword) {
                    onSubmit.invoke(password)
                } else {
                    isValid = false
                }
            }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
        }
    )
}