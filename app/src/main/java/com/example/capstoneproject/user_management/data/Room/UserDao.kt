package com.example.capstoneproject.user_management.data.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM Users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM Users WHERE userId = :id")
    fun getUser(id: Int): Flow<User>

    @Insert()
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)
}