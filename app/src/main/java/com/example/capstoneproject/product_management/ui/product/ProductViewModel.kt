package com.example.capstoneproject.product_management.ui.product

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.product_management.data.firebase.product.IProductRepository
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProductViewModel : ViewModel() {
    private lateinit var products: SnapshotStateMap<String, Product>
    private val productRepository: IProductRepository = ProductRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    val update = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): SnapshotStateMap<String, Product> {
        if (!this::products.isInitialized) {
            products = productRepository.getAll(callback = { updateLoadingState() }, update = {
                update.value = update.value.not()
                Log.e("Update", "Finished ${update.value}")
            }) {
                    result ->
                resultState.update { result }
            }
        }

        return products
    }

    fun insert(id: String? = null, product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.insert(key = id, product = product) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(key: String, product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.delete(key = key, product = product) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun setStock(key: String, value: Map<String, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.setQuantityForBranch(key = key, value = value) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun setMonthlySales(key: String, value: Map<Month, Int>, year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.setMonthlySales(key = key, value = value.mapKeys { it.key.name }, year = year) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun removeCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.removeCategory(categoryId = categoryId) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun removeBranchStock(branchId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.removeBranchStock(branchId = branchId) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getProduct(id: String?): Product? {
        return products[id]
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }

    fun getMonthlySales(date: LocalDate, product: Product): MutableCollection<Int> {
        return product.transaction.monthlySales
            .let {
                val map = mutableMapOf<String, Int>()

                for (month in 1..12) {
                    val newDate = date.minusMonths(month.toLong())
                    val value = it.getOrDefault(newDate.year.toString(), mapOf()).getOrDefault(newDate.month.name, 0)
                    map["${newDate.month} ${newDate.year}"] = value
                }

                map
            }
            .values
    }

    fun getCriticalLevel(date: LocalDate, product: Product): Double {
        val monthlySales = getMonthlySales(date = date, product = product)
        val safetyStock = ((monthlySales.maxOrNull() ?: 0).toDouble() - (monthlySales.let {
            if (it.minOrNull() != null) {
                if (it.min() == it.max()) 0 else it.min()
            } else {
                0
            }
        }))

        return ((monthlySales.sum().toDouble() / 12) + safetyStock) / 2
    }

    fun getReorderPoint(date: LocalDate, product: Product): Double {
        val monthlySales = getMonthlySales(date = date, product = product)
        val lastEditDate = Instant.ofEpochMilli(product.lastEdit as Long).atZone(ZoneId.systemDefault()).toLocalDate()
        val firstStep = monthlySales.sum().toDouble() / if (lastEditDate.isLeapYear) 366 else 365
        return (firstStep * product.leadTime) + ((monthlySales.maxOrNull() ?: 0) - (monthlySales.let {
            if (it.minOrNull() != null) {
                if (it.min() == it.max()) 0 else it.min()
            } else {
                0
            }
        }))
    }

    fun archiveItem(id: String, remove: Boolean, product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/${product.productName}_(${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}).json")
            val gson = Gson()
            val json = gson.toJson(product.copy(id = id))

            try {
                file.writeText(json)
            } catch (e: IOException) {
                resultState.update { FirebaseResult(errorMessage = e.message) }
            }

            if (remove) {
                productRepository.archiveItem(id = id) {
                    resultState.update { it }
                }
            }
        }
    }

    fun readFromJson(file: InputStream): Product {
        val gson = Gson()
        val json: String
        return try {
            json = file.bufferedReader().use { it.readText() }
            gson.fromJson(json, Product::class.java)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            Product()
        }
    }
}

