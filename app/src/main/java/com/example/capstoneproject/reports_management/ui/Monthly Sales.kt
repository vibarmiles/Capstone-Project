package com.example.capstoneproject.reports_management.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.ColorUtils
import com.example.capstoneproject.product_management.data.firebase.product.Product
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun MonthlySales(
    date: LocalDate,
    products: List<Product>
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items (11) { month ->
            val totalSales = if (month == 0) {
                products.sumOf { it.sellingPrice * it.transaction.soldThisMonth }
            } else {
                totalSalesInMonth(date.minusMonths(month.toLong()).month, products)
            }
            val previousTotalSales = totalSalesInMonth(date.minusMonths(month.toLong() + 1).month, products)
            val percent = (((totalSales - previousTotalSales) / previousTotalSales)) * 100

            Column {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "₱${String.format("%.2f", totalSales)}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Text(text = date.minusMonths(month.toLong()).format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                    },
                    trailingContent = {
                        if (previousTotalSales > 0) {
                            Text(
                                text = String.format("%.2f", abs(percent)),
                                color = if (percent < 0) MaterialTheme.colors.error else if (percent > 0) Color(ColorUtils.blendARGB(Color.Green.toArgb(), Color.Black.toArgb(), 0.2f)) else MaterialTheme.colors.onSurface,
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
    month: Month,
    products: List<Product>
): Double {
    return products.sumOf {
        it.sellingPrice * it.transaction.monthlySales.getOrDefault(month.name, 0)
    }
}