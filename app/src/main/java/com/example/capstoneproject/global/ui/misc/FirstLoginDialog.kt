package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R

@Composable
fun FirstLoginDialog(
    changePassword: Boolean,
    onSubmitWithOldPassword: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(true) }
    val localFocusManager = LocalFocusManager.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Change Password")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (changePassword) {
                    TextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text(text = "Old Password") },
                        isError = !isValid,
                        trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                        keyboardActions = KeyboardActions(
                            onNext = { localFocusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                }

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "New Password") },
                    isError = !isValid,
                    trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                    keyboardActions = KeyboardActions(
                        onNext = { localFocusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Confirm Password") },
                    isError = !isValid,
                    trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                    keyboardActions = KeyboardActions(
                        onDone = { localFocusManager.clearFocus() }
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (password == confirmPassword && password.length >= 6) {
                    if (changePassword) {
                        if (oldPassword.isNotBlank()) {
                            onSubmitWithOldPassword.invoke(oldPassword, password)
                        } else {
                            isValid = false
                        }
                    } else {
                        onSubmit.invoke(password)
                    }
                } else {
                    isValid = false
                }
            }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = if (changePassword) ({
            TextButton(colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        }) else null,
        icon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
        }
    )
}