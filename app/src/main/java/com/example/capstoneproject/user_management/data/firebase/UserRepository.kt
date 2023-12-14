package com.example.capstoneproject.user_management.data.firebase

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class UserRepository : IUserRepository {
    private val firebase = Firebase.database.reference
    private val userCollectionReference = firebase.child("users")

    override fun getAll(callback: () -> Unit, update: () -> Unit, result: (FirebaseResult) -> Unit): SnapshotStateMap<String, User> {
        val users = mutableStateMapOf<String, User>()

        userCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                users[snapshot.key!!] = snapshot.getValue<User>()!!
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                users[snapshot.key!!] = snapshot.getValue<User>()!!
                update.invoke()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                users[snapshot.key!!] = snapshot.getValue<User>()!!
                update.invoke()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                users[snapshot.key!!] = snapshot.getValue<User>()!!
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
            }
        })

        userCollectionReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
            }

        })

        return users
    }

    override fun getUser(email: String, user: (UserAccountDetails) -> Unit) {
        userCollectionReference.get().addOnSuccessListener {
            val found = it.getValue<Map<String, User>>()?.entries?.firstOrNull { foundUser -> foundUser.value.email == email }
            if (found != null) {
                Log.e("Found User", found.value.active.toString())
                userCollectionReference.child(found.key).setValue(found.value.copy(lastLogin = ServerValue.TIMESTAMP)).addOnSuccessListener {
                    userCollectionReference.child(found.key).get().addOnSuccessListener { u ->
                        u.getValue<User>()!!.let { thisUser ->
                            user.invoke(UserAccountDetails(id = found.key, branchId = thisUser.branchId, previousLoginDate = found.value.lastLogin as Long, loginDate = thisUser.lastLogin as Long, userLevel = thisUser.userLevel, isActive = thisUser.active))
                        }
                    }.addOnFailureListener { exception ->
                        user.invoke(UserAccountDetails(errorMessage = exception.message))
                    }
                }
            } else {
                user.invoke(UserAccountDetails(errorMessage = "Access Denied"))
            }
        }
    }

    override fun insert(key: String?, user: User, result: (FirebaseResult) -> Unit) {
        if (key != null) {
            userCollectionReference.child(key).setValue(user).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            userCollectionReference.push().setValue(user).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(key: String, user: User, result: (FirebaseResult) -> Unit) {
        userCollectionReference.child(key).setValue(user.copy(active = user.active.not())).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}