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
import java.time.Instant
import java.time.Month
import java.time.ZoneId


@Composable
fun ProductMonthlySalesFormScreen(
    dismissRequest: () -> Unit,
    productViewModel: ProductViewModel,
    productId: String,
    userViewModel: UserViewModel
) {
    val map = productViewModel.getProduct(productId)?.transaction?.monthlySales ?: mapOf()
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()
    val currentYear = Instant.ofEpochMilli(userAccountDetails.value.loginDate).atZone(ZoneId.systemDefault()).toLocalDate().year
    val viewModel: ProductMonthlySalesViewModel = viewModel()
    var year = Instant.ofEpochMilli(userViewModel.userAccountDetails.collectAsState().value.loginDate).atZone(ZoneId.systemDefault()).year
    val localFocusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = ("Monthly Sales").uppercase()) }, navigationIcon = {
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
            item {
                var text by remember { mutableStateOf(year.toString()) }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = {
                        if (it.length <= 4) {
                            it.toIntOrNull()?.let { y ->
                                text = it
                                year = y
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        localFocusManager.moveFocus(FocusDirection.Down)
                    })
                )
            }

            if (year in 1970..currentYear) {
                itemsIndexed(Month.values()) { index, it ->
                    var text by remember(year) { mutableStateOf(
                        map.let { yearMap ->
                            if (yearMap.containsKey(year.toString())) {
                                yearMap[year.toString()]!![it.name].toString()
                            } else {
                                ""
                            }
                        }
                    ) }
                    val isValid by remember { mutableStateOf(true) }

                    if (text.isNotBlank()) {
                        viewModel.salesPerMonth[it] = if (text.toIntOrNull() != null) text else ""
                    }

                    OutlinedTextField(
                        trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                        colors = GlobalTextFieldColors(),
                        isError = !isValid,
                        modifier = Modifier.fillMaxWidth(),
                        value = if (text.toIntOrNull() != null) text else "",
                        label = {
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
            } else {
                item {
                    Text(text = "Invalid Year")
                }
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
                            newMap[pair.key] = pair.value.toIntOrNull() ?: 0
                        }

                        productViewModel.setMonthlySales(productId, newMap, year)
                        userViewModel.log("adjust_monthly_sales")
                        dismissRequest.invoke()
                    }
                }
            }
        }
    }
}