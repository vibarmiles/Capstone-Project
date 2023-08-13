package com.example.capstoneproject.user_management.domain.repository.Room

import com.example.capstoneproject.user_management.domain.model.Room.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAll(): Flow<List<User>>

    suspend fun getEntryById(id: Int): User?

    suspend fun insertEntry(user: User)

    suspend fun deleteEntry(user: User)
}