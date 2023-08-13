package com.example.capstoneproject.user_management.data.repository.Room

import com.example.capstoneproject.user_management.data.source.Room.UserDao
import com.example.capstoneproject.user_management.domain.model.Room.User
import com.example.capstoneproject.user_management.domain.repository.Room.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImplementation(
    private val dao: UserDao
): UserRepository {
    override fun getAll(): Flow<List<User>> {
        return dao.getAll()
    }

    override suspend fun getEntryById(id: Int): User? {
        return dao.getEntryById(id)
    }

    override suspend fun insertEntry(user: User) {
        dao.insertEntry(user)
    }

    override suspend fun deleteEntry(user: User) {
        dao.deleteEntry(user)
    }
}