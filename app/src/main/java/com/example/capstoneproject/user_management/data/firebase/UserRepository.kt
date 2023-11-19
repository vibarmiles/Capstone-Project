package com.example.capstoneproject.user_management.data.firebase

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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

    override fun getUser(email: String, user: (User?) -> Unit) {
        userCollectionReference.get().addOnSuccessListener {
            user.invoke(it.getValue<Map<String, User>>()?.values?.firstOrNull { foundUser -> foundUser.email == email })
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

    override fun delete(key: String, result: (FirebaseResult) -> Unit) {
        userCollectionReference.child(key).removeValue().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}