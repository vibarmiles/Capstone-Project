package com.example.capstoneproject.global.ui.misc

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.capstoneproject.R

@Composable
fun ConfirmationDialog(
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Proceed with submission?")
        },
        text = {
            Text(text = "Are you sure you want to make this transaction?")
        },
        confirmButton = {
            Button(onClick = onSubmit) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Send, contentDescription = null)
        }
    )
}

@Composable
fun ConfirmationForAddingDialog(
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Are You Sure?")
        },
        text = {
            Text(text = "Are you sure you want to continue? Confirming this action will save the entry to the database.")
        },
        confirmButton = {
            Button(onClick = onSubmit) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.SaveAs, contentDescription = null)
        }
    )
}