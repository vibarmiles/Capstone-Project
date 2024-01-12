package com.example.capstoneproject.user_management.data.firebase

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails

interface IUserRepository {
    fun getAll(callback: () -> Unit, update: () -> Unit, result: (FirebaseResult) -> Unit): SnapshotStateMap<String, User>
    fun getUser(email: String, user: (UserAccountDetails) -> Unit)
    fun updatePassword(key: String, password: String, result: (FirebaseResult) -> Unit)
    fun insert(key: String?, user: User, result: (FirebaseResult) -> Unit)
    fun delete(key: String, user: User, result: (FirebaseResult) -> Unit)
    fun archiveItem(id: String, result: (FirebaseResult) -> Unit)
    fun getEmail(phoneNumber: String, function: (String) -> Unit)
}