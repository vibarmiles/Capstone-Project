package com.example.capstoneproject.user_management.ui.users

import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.global.data.firebase.log.ILoggingRepository
import com.example.capstoneproject.global.data.firebase.log.LoggingRepository
import com.example.capstoneproject.user_management.data.firebase.IUserRepository
import com.example.capstoneproject.user_management.data.firebase.User
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.data.firebase.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class UserAccountDetails(
    val id: String = "",
    val branchId: String? = null,
    val previousLoginDate: Long = 0,
    val loginDate: Long = 0,
    val firstLogin: Boolean = true,
    val userLevel: UserLevel = UserLevel.Cashier,
    val isActive: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel : ViewModel() {
    private lateinit var users: SnapshotStateMap<String, User>
    private val userRepository: IUserRepository = UserRepository()
    private val loggingRepository: ILoggingRepository = LoggingRepository()
    val isLoading = mutableStateOf(true)
    val update = mutableStateOf(true)
    private val userAccountDetailsState = MutableStateFlow(UserAccountDetails())
    val userAccountDetails = userAccountDetailsState.asStateFlow()
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): SnapshotStateMap<String, User> {
        if (!this::users.isInitialized) {
            isLoading.value = true
            users = userRepository.getAll(callback = { updateLoadingState() }, update = {
                update.value = update.value.not()
            }) { result ->
                resultState.update { result }
            }
        }

        return users
    }

    fun getUser(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUser(email = email) { user ->
                getAll()
                userAccountDetailsState.update { user }
            }
        }
    }

    fun getEmail(phoneNumber: String, email: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getEmail(phoneNumber = phoneNumber) {
                email.invoke(it)
            }
        }
    }

    fun getUserDetails(userId: String?): User? {
        return userId?.let { users.getValue(userId) }
    }

    fun insert(id: String?, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.insert(key = id, user = user) { result ->
                resultState.update { result }
            }
        }
    }

    fun updatePassword(id: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updatePassword(key = id, password = password) { result ->
                resultState.update { result }
            }
        }
    }

    fun updateOldPassword(password: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.authenticate(key = userAccountDetails.value.id, password = password, newPassword = newPassword) { result ->
                resultState.update { result }
            }
        }
    }

    fun delete(id: String, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.delete(key = id, user = user) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }

    fun log(event: String) {
        viewModelScope.launch(Dispatchers.IO) {
            loggingRepository.log(log = com.example.capstoneproject.global.data.firebase.log.Log(
                event = event,
                userId = userAccountDetails.value.id
            ))
        }
    }

    fun logout() {
        userAccountDetailsState.update { UserAccountDetails() }
    }

    fun archiveItem(id: String, remove: Boolean, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/${user.lastName}_${user.firstName}_(${
                    LocalDate.now().format(
                        DateTimeFormatter.ISO_LOCAL_DATE)}).json")
            val gson = Gson()
            val json = gson.toJson(user.copy(id = id))

            try {
                file.writeText(json)
            } catch (e: IOException) {
                resultState.update { FirebaseResult(errorMessage = e.message) }
            }

            if (remove) {
                userRepository.archiveItem(id = id) {
                    resultState.update { it }
                }
            }
        }
    }

    fun readFromJson(file: InputStream): User {
        val gson = Gson()
        val json: String
        return try {
            json = file.bufferedReader().use { it.readText() }
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            User()
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.resetPassword(email) {
                resultState.update { it }
            }
        }
    }
}