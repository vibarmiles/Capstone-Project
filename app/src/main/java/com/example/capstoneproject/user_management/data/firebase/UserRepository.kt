package com.example.capstoneproject.user_management.data.firebase

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.user_management.ui.users.UserAccountDetails
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class UserRepository : IUserRepository {
    private val firebase = Firebase.database.reference
    private val userCollectionReference = firebase.child("users")
    private val auth = FirebaseAuth.getInstance()

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
                userCollectionReference.child(found.key).setValue(found.value.copy(lastLogin = ServerValue.TIMESTAMP)).addOnSuccessListener {
                    userCollectionReference.child(found.key).get().addOnSuccessListener { u ->
                        u.getValue<User>()!!.let { thisUser ->
                            user.invoke(UserAccountDetails(id = found.key, branchId = thisUser.branchId, previousLoginDate = found.value.lastLogin as Long, loginDate = thisUser.lastLogin as Long, firstLogin = thisUser.firstLogin, userLevel = thisUser.userLevel, isActive = thisUser.active))
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

    override fun resetPassword(email: String, result: (FirebaseResult) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            result.invoke(FirebaseResult(errorMessage = "Email Sent!"))
        }
    }

    override fun updatePassword(key: String, password: String, result: (FirebaseResult) -> Unit) {
        userCollectionReference.child(key).child("firstLogin").setValue(false).addOnSuccessListener {
            auth.currentUser?.updatePassword(password)
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(errorMessage = it.message))
        }
    }

    override fun authenticate(
        key: String,
        password: String,
        newPassword: String,
        result: (FirebaseResult) -> Unit
    ) {
        auth.currentUser.also {
            if (it != null) {
                it.reauthenticate(EmailAuthProvider.getCredential(it.email!!, password)).addOnSuccessListener {
                    updatePassword(key = key, password = newPassword, result = result)
                }.addOnFailureListener { e ->
                    result.invoke(FirebaseResult(errorMessage = e.message))
                }
            } else {
                result.invoke(FirebaseResult(errorMessage = "Not Logged In!"))
            }
        }
    }

    override fun insert(key: String?, user: User, result: (FirebaseResult) -> Unit) {
        if (key != null) {
            userCollectionReference.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentUsers = currentData.getValue<Map<String, User>>()?.filterKeys { it != key } ?: mapOf()

                    if (currentUsers.any { it.value.email == user.email || it.value.phoneNumber == user.phoneNumber }) {
                        return Transaction.abort()
                    }

                    currentData.child(key).value = user.copy(password = null)
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (!committed) {
                        result.invoke(FirebaseResult(errorMessage = "Error has occurred. Please check current users then try again later!"))
                    } else {
                        auth.createUserWithEmailAndPassword(user.email, user.password!!)
                        result.invoke(FirebaseResult(result = true))
                    }
                }

            })
        } else {
            userCollectionReference.push().setValue(user.copy(password = null)).addOnSuccessListener {
                auth.createUserWithEmailAndPassword(user.email, user.password!!)
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

    override fun archiveItem(id: String, result: (FirebaseResult) -> Unit) {
        userCollectionReference.child(id).removeValue().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(errorMessage = it.message))
        }
    }

    override fun getEmail(phoneNumber: String, function: (String) -> Unit) {
        userCollectionReference.get().addOnSuccessListener {
            val found = it.getValue<Map<String, User>>()?.entries?.firstOrNull { foundUser -> foundUser.value.phoneNumber == phoneNumber }
            function.invoke(found?.value?.email ?: "")
        }
    }
}