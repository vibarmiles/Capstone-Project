package com.example.capstoneproject.reports_management.ui

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.R
import com.example.capstoneproject.product_management.data.firebase.product.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class ReportsViewModel(application: Application) : AndroidViewModel(application = application) {
    fun generateFSNReport(
        products: List<Pair<Double, Map.Entry<String, Product>>>,
        callback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val productList = products
                .sortedBy { it.second.value.productName.uppercase() }
                .sortedByDescending { it.first }
                .chunked(35)

            val pdfDocument = PdfDocument()

            val paint = Paint()
            val text = Paint()
            val line = Paint()

            val startY = 350f

            text.color = Color.Black.toArgb()
            text.textSize = 25f

            line.strokeWidth = 2f

            productList.forEachIndexed { index, pair ->
                val myPageInfo = PdfDocument.PageInfo.Builder(1080, 1920, index + 1).create()
                val myPage = pdfDocument.startPage(myPageInfo)
                val canvas = myPage.canvas

                canvas.drawBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.app_icon), 50f, 50f, paint)
                canvas.drawText("Sam Speed Motorcycle Parts and Accessories", 239f, 120f, text)
                canvas.drawText("Inventory Management System", 239f, 150f, text)
                canvas.drawText("FSN Analysis", 239f, 180f, text)

                canvas.drawLine(50f, startY - 25, 1030f, startY - 25, line)
                canvas.drawText("Products", 60f, startY, text)
                canvas.drawText("ITR", 830f, startY, text)
                canvas.drawText("Class", 930f, startY, text)
                canvas.drawLine(50f, startY + 10, 1030f, startY + 10, line)

                pair.forEachIndexed { index2, pair2 ->
                    canvas.drawText(pair2.second.value.productName, 60f, startY + (index2 * 35) + 35, text)
                    canvas.drawText(String.format("%.2f", pair2.first), 830f, startY + (index2 * 35) + 35, text)
                    canvas.drawText(if (pair2.first > 3) "Fast" else if (pair2.first in 1.0..3.0) "Slow" else "Non", 930f, startY + (index2 * 35) + 35, text)
                    canvas.drawLine(50f, startY + (index2 * 35) + 45, 1030f, startY + (index2 * 35) + 45, line)
                }

                pdfDocument.finishPage(myPage)
            }

            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/FSN_Analysis_(${LocalDate.now()}).pdf")

            try {
                pdfDocument.writeTo(FileOutputStream(file))
            } catch (e: IOException) {
                Log.e("IO Exception", e.message.toString())
            }

            pdfDocument.close()
            callback.invoke()
        }
    }

    fun generateInventoryReport(
        products: Map<String, Product>,
        callback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val productList = products
                .toList()
                .groupBy { it.second.productName }
                .map { map -> map.key to map.value.sumOf { it.second.stock.values.sum() } }
                .sortedBy { it.first.uppercase() }
                .sortedByDescending { it.first }
                .chunked(35)

            val pdfDocument = PdfDocument()

            val paint = Paint()
            val text = Paint()
            val line = Paint()

            val startY = 350f

            text.color = Color.Black.toArgb()
            text.textSize = 25f

            line.strokeWidth = 2f

            productList.forEachIndexed { index, chunk ->
                val myPageInfo = PdfDocument.PageInfo.Builder(1080, 1920, index + 1).create()
                val myPage = pdfDocument.startPage(myPageInfo)
                val canvas = myPage.canvas

                canvas.drawBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.app_icon), 50f, 50f, paint)
                canvas.drawText("Sam Speed Motorcycle Parts and Accessories", 239f, 120f, text)
                canvas.drawText("Inventory Management System", 239f, 150f, text)
                canvas.drawText("Inventory Report", 239f, 180f, text)

                canvas.drawLine(50f, startY - 25, 1030f, startY - 25, line)
                canvas.drawText("Products", 60f, startY, text)
                canvas.drawText("Quantity", 830f, startY, text)
                canvas.drawLine(50f, startY + 10, 1030f, startY + 10, line)

                chunk.forEachIndexed { index2, pair ->
                    canvas.drawText(pair.first, 60f, startY + (index2 * 35) + 35, text)
                    canvas.drawText(pair.second.toString(), 830f, startY + (index2 * 35) + 35, text)
                    canvas.drawLine(50f, startY + (index2 * 35) + 45, 1030f, startY + (index2 * 35) + 45, line)
                }

                pdfDocument.finishPage(myPage)
            }

            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/Inventory_Report_(${LocalDate.now()}).pdf")

            try {
                pdfDocument.writeTo(FileOutputStream(file))
            } catch (e: IOException) {
                Log.e("IO Exception", e.message.toString())
            }

            pdfDocument.close()
            callback.invoke()
        }
    }

    fun generateMonthlySalesReport(
        date: LocalDate,
        products: List<Product>,
        callback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext

            val pdfDocument = PdfDocument()

            val paint = Paint()
            val text = Paint()
            val error = Paint()
            val line = Paint()

            val startY = 350f

            text.color = Color.Black.toArgb()
            text.textSize = 25f

            error.color = Color.Red.toArgb()
            error.textSize = 25f

            line.strokeWidth = 2f

            (0..(ChronoUnit.MONTHS.between(date.minusYears(5), date).toInt())).chunked(35).forEachIndexed { index, list ->
                val myPageInfo = PdfDocument.PageInfo.Builder(1080, 1920, index + 1).create()
                val myPage = pdfDocument.startPage(myPageInfo)
                val canvas = myPage.canvas

                canvas.drawBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.app_icon), 50f, 50f, paint)
                canvas.drawText("Sam Speed Motorcycle Parts and Accessories", 239f, 120f, text)
                canvas.drawText("Inventory Management System", 239f, 150f, text)
                canvas.drawText("Monthly Sales", 239f, 180f, text)

                canvas.drawLine(50f, startY - 25, 1030f, startY - 25, line)
                canvas.drawText("Month Year", 60f, startY, text)
                canvas.drawText("Amount", 380f, startY, text)
                canvas.drawText("% Increase", 700f, startY, text)
                canvas.drawLine(50f, startY + 10, 1030f, startY + 10, line)

                list.forEachIndexed { index2, month ->
                    val totalSales = if (month == 0) {
                        products.sumOf { it.sellingPrice * it.transaction.soldThisMonth }
                    } else {
                        totalSalesInMonth(date.minusMonths(month.toLong()), products)
                    }

                    val previousTotalSales = totalSalesInMonth(date.minusMonths(month.toLong() + 1), products)
                    val percent = (((totalSales - previousTotalSales) / previousTotalSales)) * 100
                    canvas.drawText("${date.minusMonths(month.toLong()).month} ${date.minusMonths(month.toLong()).year}", 60f, startY + (index2 * 35) + 35, text)
                    canvas.drawText("₱${String.format("%,.2f", totalSales)}", 380f, startY + (index2 * 35) + 35, text)
                    if (previousTotalSales > 0) {
                        canvas.drawText(
                            String.format("%,.2f", abs(percent)) + "%",
                            700f,
                            startY + (index2 * 35) + 35,
                            if (percent < 0) error else text

                        )
                    }
                    canvas.drawLine(50f, startY + (index2 * 35) + 45, 1030f, startY + (index2 * 35) + 45, line)
                }

                pdfDocument.finishPage(myPage)
            }

            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/Monthly_Sales_Report(${LocalDate.now()}).pdf")

            try {
                pdfDocument.writeTo(FileOutputStream(file))
            } catch (e: IOException) {
                Log.e("IO Exception", e.message.toString())
            }

            pdfDocument.close()
            callback.invoke()
        }
    }
}