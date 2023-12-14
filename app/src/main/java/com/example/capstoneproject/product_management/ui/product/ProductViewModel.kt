package com.example.capstoneproject.product_management.ui.product

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.Month
import java.time.ZoneId

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

    fun getMonthlySales(product: Product): MutableCollection<Int> {
        var count = 1
        return product.transaction.monthlySales
            .toList()
            .sortedByDescending { it.first }
            .let {
                val map = mutableMapOf<String, Int>()
                it.forEach { currentMap ->
                    if (count > 12) {
                        return@forEach
                    }

                    for (entry in currentMap.second.mapKeys { entry -> Month.values().first { month -> month.name == entry.key } }.toList().sortedBy { item -> item.first }) {
                        if (count <= 12) {
                            count += 1
                        } else {
                            break
                        }
                        map["${entry.first} ${currentMap.first}"] = entry.second
                    }
                }
                map
            }
            .values
    }

    fun getCriticalLevel(product: Product): Double {
        val monthlySales = getMonthlySales(product)
        val safetyStock = ((monthlySales.maxOrNull() ?: 0).toDouble() - (monthlySales.let {
            if (it.minOrNull() != null) {
                if (it.min() == it.max()) 0 else it.min()
            } else {
                0
            }
        })) / 2

        return ((monthlySales.sum().toDouble() / 12) + safetyStock)
    }

    fun getReorderPoint(product: Product): Double {
        val monthlySales = getMonthlySales(product)
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
}

