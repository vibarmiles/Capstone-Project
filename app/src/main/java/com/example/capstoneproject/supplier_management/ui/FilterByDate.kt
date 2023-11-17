package com.example.capstoneproject.supplier_management.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterByDate(
    onClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp), expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        var textFieldValue by remember { mutableStateOf("Today") }
        val dropdownMenuItems = listOf("Today", "Last 3 days", "Last 7 days", "Last 30 days")
        OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = textFieldValue, readOnly = true, onValueChange = {  })
        DropdownMenu(modifier = Modifier
            .exposedDropdownSize()
            .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
            dropdownMenuItems.forEachIndexed {
                    index, s ->
                DropdownMenuItem(text = { Text(text = s) }, onClick = { onClick.invoke(index); textFieldValue = s; expanded = false })
            }
        }
    }
}