package com.example.capstoneproject.global.ui.misc

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Block
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R

@Composable
fun MakeInactiveDialog(
    item: String,
    function: String,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Make $function")
        },
        text = {
            Text(text = "Are you sure you want to make $item ${function.lowercase()}?")
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
            Icon(imageVector = if (function == "Inactive") Icons.Outlined.Block else Icons.Default.Add, contentDescription = null)
        }
    )
}