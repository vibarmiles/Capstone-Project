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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.product_management.data.Room.product.Product
import com.example.capstoneproject.product_management.ui.category.viewmodel.CategoryViewModel
import com.example.capstoneproject.product_management.ui.product.viewModel.ProductViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductFormSreen(function: String, viewModel: ProductViewModel, back: () -> Unit) {
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
            val category = viewModel.categories.collectAsState(initial = listOf())
            var name by remember { mutableStateOf("") }
            var isNameValid by remember { mutableStateOf(true) }
            var price by remember { mutableStateOf("") }
            var isPriceValid by remember { mutableStateOf(true) }
            var expanded by remember { mutableStateOf(false) }
            var categoryId: Int by remember { mutableStateOf(0) }
            var selectedCategory by remember { mutableStateOf("None") }

            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = name, onValueChange = { name = it }, placeholder = { Text(text = "Enter Product Name") }, label = { Text(text = "Product Name") }, isError = !isNameValid, trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) })
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = price, onValueChange = { price = it }, placeholder = { Text(text = "Enter Selling Price") }, label = { Text(text = "Selling Price") }, isError = !isPriceValid, trailingIcon = { if (!isPriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = selectedCategory, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.category)) })

                DropdownMenu(modifier = Modifier.exposedDropdownSize().fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                    androidx.compose.material3.DropdownMenuItem(text = { Text(text = "None") }, onClick = { categoryId = 0; selectedCategory = "None" })

                    category.value.forEach {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(text = it.categoryName) }, onClick = { categoryId = it.id; selectedCategory = it.categoryName })
                    }
                }
            }
            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isPriceValid = if (price.isNotBlank()) price.toDouble() > 0 else false

                if (isNameValid && isPriceValid) {
                    viewModel.insert(product = Product(productName = name, price = price.toDouble(), category = categoryId))
                    back.invoke()
                }
            }
        }
    }
}