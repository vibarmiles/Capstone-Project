package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel

@Composable
fun ProductQuantityFormScreen(dismissRequest: () -> Unit, productViewModel: ProductViewModel, branchViewModel: BranchViewModel, productId: String, map: Map<String, Int>? = null) {
    val branches = branchViewModel.getAll().observeAsState(listOf())
    val viewModel: BranchQuantityViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Stock Adjustment") }, navigationIcon = {
                IconButton(onClick = dismissRequest) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
            paddingValues ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(branches.value) {
                    _, it ->
                var text by rememberSaveable { mutableStateOf(if (map != null) if (map.containsKey(it.id)) map[it.id].toString() else "" else "")}
                var isValid by remember { mutableStateOf(true) }
                if (text.isNotBlank()) {
                    viewModel.stockPerBranch[it.id] = text
                } else {
                    viewModel.stockPerBranch[it.id] = "0"
                }
                androidx.compose.material3.OutlinedTextField(supportingText = { if (!isValid) Text(text = "Enter whole numbers only!", color = Color.Red) }, trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, isError = !isValid, modifier = Modifier.fillMaxWidth(), value = text, label = { Text(text = it.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }, placeholder = { Text(text = "Insert Current Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), onValueChange = { value -> text = value; viewModel.stockPerBranch[it.id] = text; isValid = text.isDigitsOnly() })
            }
            item {
                FormButtons(cancel = dismissRequest) {
                    for (pair in viewModel.stockPerBranch) {
                        viewModel.checkInput[pair.key] = pair.value.isDigitsOnly()
                    }
                    val check = !viewModel.checkInput.containsValue(false)
                    val newMap = mutableMapOf<String, Int>()
                    if (check) {
                        for (pair in viewModel.stockPerBranch) {
                            newMap[pair.key] = pair.value.toInt()
                        }
                        productViewModel.setStock(productId, newMap)
                        dismissRequest.invoke()
                    }
                }
            }
        }
    }
}