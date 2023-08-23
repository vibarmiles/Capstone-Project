package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.FormButtons

@Composable
fun ProductFormSreen(function: String, back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "$function Product") }, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
        it -> it
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            var name by remember { mutableStateOf("") }
            var isNameValid by remember { mutableStateOf(true) }
            var price by remember { mutableStateOf("") }
            var isPriceValid by remember { mutableStateOf(true) }
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = name, onValueChange = { name = it }, placeholder = { Text(text = "Enter Product Name") }, label = { Text(text = "Product Name") }, isError = !isNameValid, trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) })
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = price, onValueChange = { price = it }, placeholder = { Text(text = "Enter Selling Price") }, label = { Text(text = "Selling Price") }, isError = !isPriceValid, trailingIcon = { if (!isPriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isPriceValid = if (price.isNotBlank()) price.toInt() > 0 else false

                if (isNameValid && isPriceValid) {
                    back.invoke()
                }
            }
        }
    }
}