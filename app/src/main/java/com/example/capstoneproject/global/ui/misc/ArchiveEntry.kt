package com.example.capstoneproject.global.ui.misc

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.capstoneproject.R

@Composable
fun ArchiveEntry(
    name: String,
    isActive: Boolean,
    onSubmit: () -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    if (!isActive) {
        androidx.compose.material3.DropdownMenuItem(
            text = { Text(text = "Archive Item") },
            onClick = {
                showDialog.value = true
            },
            leadingIcon = { Icon(imageVector = Icons.Default.Archive, contentDescription = null) }
        )
    }

    if (showDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(text = "Are You Sure?")
            },
            text = {
                Text(text = "Are you sure you want to continue? Confirming this action will archive $name and remove it from the database.")
            },
            confirmButton = {
                Button(onClick = onSubmit) {
                    Text(text = stringResource(id = R.string.submit_button))
                }
            },
            dismissButton = {
                TextButton(colors = ButtonDefaults.buttonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), onClick = { showDialog.value = false }) {
                    Text(text = stringResource(id = R.string.cancel_button))
                }
            },
            icon = {
                Icon(imageVector = Icons.Default.Archive, contentDescription = null)
            }
        )
    }
}