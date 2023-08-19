package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R

@Composable
fun BranchFormScreen(function: String = "Add", back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "$function Branch")}, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
        it -> it
        var name by rememberSaveable { mutableStateOf("") }
        var address by rememberSaveable { mutableStateOf("") }
        Column(modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(text = "Branch Name") }, placeholder = { Text(text = "Enter Branch Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text(text = "Branch Address") }, placeholder = { Text(text = "Enter Branch Address") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = back, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Color.Black), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
                    Text(text = stringResource(id = R.string.cancel_button))
                }

                Button(onClick = { back.invoke() }, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(id = R.string.submit_button))
                }
            }
        }
    }
}