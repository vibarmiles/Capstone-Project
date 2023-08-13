package com.example.capstoneproject.user_management.data.source.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.example.capstoneproject.user_management.domain.model.Room.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM Users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM Users WHERE id = :id")
    suspend fun getEntryById(id: Int): User?

    @Insert(onConflict = REPLACE)
    suspend fun insertEntry(user: User)

    @Delete
    suspend fun deleteEntry(user: User)
}