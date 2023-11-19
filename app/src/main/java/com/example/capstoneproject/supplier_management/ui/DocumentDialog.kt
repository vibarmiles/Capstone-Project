package com.example.capstoneproject.supplier_management.ui

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.capstoneproject.R

sealed class Document(val doc: String) {
    object PO : Document("Purchase Order")
    object RO : Document("Return Order")
    object TO : Document("Transfer Order")
}

@Composable
fun DocumentDialog(
    action: String,
    type: Document,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Are you sure?")
        },
        text = {
            Text(text = "Pressing Submit would change the status of the ${type.doc.lowercase()} to $action. This cannot be changed again after proceeding.")
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
        }
    )
}