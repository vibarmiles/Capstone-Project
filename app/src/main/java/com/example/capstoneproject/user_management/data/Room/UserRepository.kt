package com.example.capstoneproject.user_management.data.Room

import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val dao: UserDao
) {
    fun getAll(): Flow<List<User>> = dao.getAll()
    fun getUserById(id: Int): Flow<User?> = dao.getUser(id)
    suspend fun insertUser(user: User) = dao.insert(user)
    suspend fun deleteUser(user: User) = dao.delete(user)
    suspend fun updateUser(user: User) = dao.update(user)
}