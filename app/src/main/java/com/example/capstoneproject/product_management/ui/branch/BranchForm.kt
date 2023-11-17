package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.product_management.data.firebase.branch.Branch

@Composable
fun BranchFormScreen(
    viewModel: BranchViewModel,
    function: String = "Add",
    id: String? = null,
    back: () -> Unit
) {
    val branch = viewModel.getBranch(id) ?: Branch()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "$function Branch")}, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
            paddingValues ->
        var name by remember { mutableStateOf(branch.name) }
        var address by remember { mutableStateOf(branch.address) }
        var isNameValid by remember { mutableStateOf(true) }
        var isAddressValid by remember { mutableStateOf(true) }
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(text = "Branch Name") }, placeholder = { Text(text = "Enter Branch Name") }, isError = !isNameValid, modifier = Modifier.fillMaxWidth(), trailingIcon = { if (!isNameValid) Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color.Red
            )})
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text(text = "Branch Address") }, placeholder = { Text(text = "Enter Branch Address") }, isError = !isAddressValid, modifier = Modifier.fillMaxWidth(), trailingIcon = { if (!isAddressValid) Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color.Red
            )})
            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isAddressValid = address.isNotBlank()

                if (isNameValid && isAddressValid) {
                    viewModel.insert(branch.copy(name = name, address = address))
                    back.invoke()
                }
            }
        }
    }
}