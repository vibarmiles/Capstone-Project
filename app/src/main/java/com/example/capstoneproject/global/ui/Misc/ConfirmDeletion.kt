package com.example.capstoneproject.global.ui.Misc

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R

@Composable
fun ConfirmDeletion(item: String, onCancel: () -> Unit, onSubmit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Confirm delete", fontSize = 24.sp)
        },
        text = {
            Text(text = "Are you sure you want to delete $item?")
        },
        confirmButton = {
            Button(onClick = onSubmit) {
                Text(text = stringResource(id = R.string.submit_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel_button))
            }
        }
    )
}