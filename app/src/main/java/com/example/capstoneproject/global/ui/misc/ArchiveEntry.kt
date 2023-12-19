package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R

@Composable
fun ArchiveEntry(
    name: String,
    isActive: Boolean,
    onSubmit: (Boolean) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }

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
                Column {
                    Text(text = "Are you sure you want to continue? Confirming this action will archive $name.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        Text(text = "Remove item from database.")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onSubmit.invoke(checked) }) {
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