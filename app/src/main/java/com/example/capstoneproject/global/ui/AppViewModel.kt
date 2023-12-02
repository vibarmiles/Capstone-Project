package com.example.capstoneproject.global.ui

import androidx.lifecycle.ViewModel
import com.example.capstoneproject.global.data.firebase.ConnectionRepository
import com.example.capstoneproject.login.data.login.SignInResult
import com.example.capstoneproject.product_management.data.firebase.product.IProductRepository
import com.example.capstoneproject.product_management.data.firebase.product.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class AppUiState(
    val lockMenu: Boolean = true,
    val loadLogin: Boolean = false,
    val showProgress: Boolean = false,
    val loadingProgress: Float = 0f,
    val user: SignInResult = SignInResult(data = null, errorMessage = null)
)

class AppViewModel : ViewModel() {
    private val connectionRepository: ConnectionRepository = ConnectionRepository()
    private val productRepository: IProductRepository = ProductRepository()
    private val appUiState = MutableStateFlow(AppUiState())
    val connection = connectionRepository.connection
    val uiState = appUiState.asStateFlow()

    fun updateMonthlyCounters(previous: LocalDate, current: LocalDate) {
        if (previous.month != current.month || previous.year != current.year) {
            productRepository.checkDate(current) { progress ->
                (progress != 1f).let { progressing ->
                    if (progressing) {
                        appUiState.update { it.copy(showProgress = true, loadingProgress = progress) }
                    } else {
                        appUiState.update { it.copy(showProgress = false, loadingProgress = progress, lockMenu = false) }
                    }
                }
            }
        } else {
            appUiState.update { it.copy(lockMenu = false) }
        }
    }

    fun updateUser(user: SignInResult) = appUiState.update { it.copy(user = user) }

    fun loadLogin(load: Boolean) = appUiState.update { it.copy(loadLogin = load) }

    fun resetUiState() = appUiState.update { AppUiState() }
}