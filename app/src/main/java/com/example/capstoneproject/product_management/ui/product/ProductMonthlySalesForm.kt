package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import java.time.Month


@Composable
fun ProductMonthlySalesFormScreen(
    dismissRequest: () -> Unit,
    productViewModel: ProductViewModel,
    productId: String,
    userViewModel: UserViewModel
) {
    val map = productViewModel.getProduct(productId)?.transaction?.monthlySales ?: mapOf()
    val viewModel: ProductMonthlySalesViewModel = viewModel()
    val localFocusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = ("Stock Adjustment").uppercase()) }, navigationIcon = {
                IconButton(onClick = dismissRequest) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(Month.values()) { index, it ->
                var text by remember { mutableStateOf(if (map.containsKey(it.name)) map[it.name].toString() else "") }
                val isValid by remember { mutableStateOf(true) }

                OutlinedTextField(
                    trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                    colors = GlobalTextFieldColors(),
                    isError = !isValid,
                    modifier = Modifier.fillMaxWidth(),
                    value = text, label = {
                        Text(text = it.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    placeholder = { Text(text = "Insert Current Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (Month.values().lastIndex == index) ImeAction.Done else ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            localFocusManager.clearFocus()
                        },
                        onNext = {
                            localFocusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let{ num ->
                            if (num >= 0) text = value
                            viewModel.salesPerMonth[it] = text
                        } ?: run {
                            if (value.isBlank()) {
                                text = ""
                                viewModel.salesPerMonth.remove(it)
                            }
                        }
                    }
                )
            }

            item {
                FormButtons(cancel = dismissRequest) {
                    for (pair in viewModel.salesPerMonth) {
                        viewModel.checkInput[pair.key] = pair.value.isDigitsOnly()
                    }
                    val check = !viewModel.checkInput.containsValue(false)
                    val newMap = mutableMapOf<Month, Int>()
                    if (check) {
                        for (pair in viewModel.salesPerMonth) {
                            newMap[pair.key] = pair.value.toInt()
                        }
                        productViewModel.setMonthlySales(productId, newMap)
                        userViewModel.log("adjust_monthly_sales")
                        dismissRequest.invoke()
                    }
                }
            }
        }
    }
}