package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.capstoneproject.R

@Composable
fun FirstLoginDialog(
    onSubmit: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {  },
        title = {
            Text(text = "Change Password")
        },
        text = {
            TextField(
                value = value,
                onValueChange = { value = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation()
            )
        },
        confirmButton = {
            Button(onClick = { onSubmit.invoke(value) }) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
        }
    )
}