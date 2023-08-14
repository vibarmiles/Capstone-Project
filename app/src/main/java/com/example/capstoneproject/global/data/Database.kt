package com.example.capstoneproject.global.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.capstoneproject.user_management.data.Room.UserDao
import com.example.capstoneproject.user_management.data.Room.User

@Database(
    entities = [User::class],
    version = 1
)
abstract class Database: RoomDatabase() {
    abstract fun getUserDao(): UserDao

    companion object {
        private var Instance: com.example.capstoneproject.global.data.Database? = null

        fun getDatabase(context: Context): com.example.capstoneproject.global.data.Database {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, com.example.capstoneproject.global.data.Database::class.java, "App_Database").build().also { Instance = it}
            }
        }
    }
}