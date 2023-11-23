package com.example.capstoneproject.user_management.ui.users

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.user_management.data.firebase.IUserRepository
import com.example.capstoneproject.user_management.data.firebase.User
import com.example.capstoneproject.user_management.data.firebase.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private lateinit var users: SnapshotStateMap<String, User>
    lateinit var id: String
    private val userRepository: IUserRepository = UserRepository()
    val isLoading = mutableStateOf(true)
    val update = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): SnapshotStateMap<String, User> {
        if (!this::users.isInitialized) {
            users = userRepository.getAll(callback = { updateLoadingState() }, update = {
                update.value = update.value.not()
                Log.d("Update", "Finished")
            }) {
                    result ->
                resultState.update { result }
            }
        }

        return users
    }

    fun getUser(email: String, authorizationCallback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUser(email = email) {
                authorizationCallback.invoke(it != null)
                getAll()
                id = it ?: ""
            }
        }
    }

    fun getUserDetails(userId: String?): User? {
        return userId?.let { users.getValue(userId) }
    }

    fun insert(id: String?, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.insert(key = id, user = user) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.delete(key = id) {
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
}