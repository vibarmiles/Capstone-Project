package com.example.capstoneproject.supplier_management.ui

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Task
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.capstoneproject.R
import com.example.capstoneproject.supplier_management.data.firebase.Status

sealed class Document(val doc: String) {
    object PO : Document("Purchase Order")
    object RO : Document("Return Order")
    object TO : Document("Transfer Order")
}

@Composable
fun DocumentDialog(
    action: Status,
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
            Text(
                buildAnnotatedString {
                    append(text = "Pressing submit would change the status of the ${type.doc.lowercase()} to ")
                    withStyle(style = SpanStyle(color = when (action) {
                        Status.COMPLETE -> Color(red = 0f, green = 0.8f, blue = 0f)
                        Status.CANCELLED -> MaterialTheme.colors.error
                        else -> MaterialTheme.colors.onSurface
                    })) {
                        append(text = action.name)
                    }
                    append(text = ". This cannot be changed again after proceeding.")
                }
            )
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
            Icon(
                imageVector = when (action) {
                    Status.COMPLETE -> Icons.Default.Task
                    Status.CANCELLED -> Icons.Default.RemoveCircleOutline
                    else -> Icons.Default.Error
                },
                contentDescription = null
            )
        }
    )
}