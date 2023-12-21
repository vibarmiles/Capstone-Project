package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.ui.theme.success
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun MonthlySales(
    date: LocalDate,
    products: List<Product>,
    showData: (String, Int) -> Unit,
) {
    val isFromDateValid = remember { mutableStateOf(true) }
    val isToDateValid = remember { mutableStateOf(true) }
    val fromDate = remember { mutableStateOf(date.minusMonths(11)) }
    val toDate = remember { mutableStateOf(date) }
    val from = remember { mutableStateOf(date.minusMonths(11)) }
    val to = remember { mutableStateOf(date) }
    val fromDateTextFieldValue = remember { mutableStateOf("${fromDate.value.month.value.toString().padStart(2, '0')}/${fromDate.value.year.toString().padStart(4, '0')}") }
    val toDateTextFieldValue = remember { mutableStateOf("${toDate.value.month.value.toString().padStart(2, '0')}/${toDate.value.year.toString().padStart(4, '0')}") }
    val focusRequester = remember { FocusRequester() }
    val localFocusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val isFocused = remember { mutableStateOf(false) }
    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex != 0 } }

    if (firstVisible && isFocused.value) {
        localFocusManager.clearFocus()
        isFocused.value = false
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier
                .background(color = MaterialTheme.colors.surface)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused.value = it.isFocused
                }
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                OutlinedTextField(
                    trailingIcon = {
                        if (fromDateTextFieldValue.value.isNotEmpty()) {
                            IconButton(onClick = {
                                fromDateTextFieldValue.value = ""
                                fromDate.value = date
                                localFocusManager.clearFocus()
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    label = { Text(text = "From this Date", color = MaterialTheme.colors.onSurface) },
                    placeholder = { Text(text = "mm/yyyy", color = MaterialTheme.colors.onSurface) },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) },
                    value = fromDateTextFieldValue.value,
                    isError = !isFromDateValid.value,
                    onValueChange = {
                        if (it.length <= 7) {
                            fromDateTextFieldValue.value = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        val newDate = fromDateTextFieldValue.value
                        if (newDate[2] == '/' && newDate.length == 7) {
                            val month = newDate.substring(0, 2).toIntOrNull()
                            val year = newDate.substringAfter('/').toIntOrNull()
                            isFromDateValid.value = Month.values().any { it.value == month } && (2020..date.year).any { it == year }
                            if (isFromDateValid.value) {
                                fromDate.value = LocalDate.of(year!!, month!!, 1)
                                localFocusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    })
                )
                OutlinedTextField(
                    trailingIcon = {
                        if (fromDateTextFieldValue.value.isNotEmpty()) {
                            IconButton(onClick = {
                                toDateTextFieldValue.value = ""
                                toDate.value = date.minusMonths(11)
                                localFocusManager.clearFocus()
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    label = { Text(text = "To this Date", color = MaterialTheme.colors.onSurface) },
                    placeholder = { Text(text = "mm/yyyy", color = MaterialTheme.colors.onSurface) },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isToDateValid.value,
                    leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) },
                    value = toDateTextFieldValue.value,
                    onValueChange = {
                        if (it.length <= 7) {
                            toDateTextFieldValue.value = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        val newDate = toDateTextFieldValue.value
                        if (newDate[2] == '/' && newDate.length == 7) {
                            val month = newDate.substring(0, 2).toIntOrNull()
                            val year = newDate.substringAfter('/').toIntOrNull()
                            isToDateValid.value = Month.values().any { it.value == month } && (2020..date.year).any { it == year }
                            if (isToDateValid.value) {
                                val currentDate = LocalDate.of(year!!, month!!, 1)
                                toDate.value = currentDate
                                localFocusManager.clearFocus()
                                if (fromDate.value < currentDate) {
                                    from.value = fromDate.value
                                    to.value = toDate.value
                                } else {
                                    isFromDateValid.value = false
                                    isToDateValid.value = false
                                }
                            }
                        }
                    })
                )
            }
        }
        items (ChronoUnit.MONTHS.between(from.value.minusMonths(1), to.value).toInt()) { month ->
            val totalSales = if (month == 0 && date == to.value) {
                products.sumOf { it.sellingPrice * it.transaction.soldThisMonth }
            } else {
                totalSalesInMonth(to.value.minusMonths(month.toLong()), products)
            }
            val previousTotalSales = totalSalesInMonth(to.value.minusMonths(month.toLong() + 1), products)
            val percent = (((totalSales - previousTotalSales) / previousTotalSales)) * 100

            Column(
                modifier = Modifier
                    .clickable {
                        showData.invoke(date.minusMonths(month.toLong()).month.name, to.value.minusMonths(month.toLong()).year)
                    }
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "₱${String.format("%,.2f", totalSales)}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Text(text = to.value.minusMonths(month.toLong()).format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                    },
                    trailingContent = {
                        if (previousTotalSales > 0) {
                            Text(
                                text = String.format("%,.2f", abs(percent)) + "%",
                                color = if (percent < 0) MaterialTheme.colors.error else if (percent > 0) success else MaterialTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
                Divider()
            }
        }
    }
}

fun totalSalesInMonth(
    date: LocalDate,
    products: List<Product>
): Double {
    return products.sumOf {
        it.sellingPrice * it.transaction.monthlySales.getOrDefault(date.year.toString(), mapOf()).getOrDefault(date.month.name, 0)
    }
}