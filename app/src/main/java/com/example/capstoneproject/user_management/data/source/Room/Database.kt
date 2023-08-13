package com.example.capstoneproject.user_management.data.source.Room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.capstoneproject.user_management.domain.model.Room.User

@Database(
    entities = [User::class],
    version = 1
)
abstract class Database: RoomDatabase() {
    abstract val userDao: UserDao
}