package com.example.capstoneproject.user_management.data.firebase

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface IUserRepository {
    fun getAll(callback: () -> Unit, update: () -> Unit, result: (FirebaseResult) -> Unit): SnapshotStateMap<String, User>
    fun getUser(email: String, user: (String?) -> Unit)
    fun insert(key: String?, user: User, result: (FirebaseResult) -> Unit)
    fun delete(key: String, result: (FirebaseResult) -> Unit)
}