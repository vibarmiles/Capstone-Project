package com.example.capstoneproject.user_management.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.Database
import com.example.capstoneproject.user_management.data.Room.User
import com.example.capstoneproject.user_management.data.Room.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    val users: Flow<List<User>>
    private val repository: UserRepository

    init {
        val userDao = Database.getDatabase(application).getUserDao()
        repository = UserRepository(userDao)
        users = repository.getAll()
    }

    fun insert(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUser(user)
        }
    }

    fun delete(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user)
        }
    }

    fun update(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
        }
    }
}