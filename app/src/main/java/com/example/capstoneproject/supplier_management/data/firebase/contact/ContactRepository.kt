package com.example.capstoneproject.supplier_management.data.firebase.contact

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ContactRepository {
    private val firebase = Firebase.database.reference
    private val contactCollectionReference = firebase.child("contacts")

    fun getAll(): SnapshotStateMap<String, Contact> {
        val contacts = mutableStateMapOf<String, Contact>()

        contactCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                contacts[snapshot.key!!] = snapshot.getValue<Contact>()!!
                Log.d("Added", snapshot.value.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                contacts[snapshot.key!!] = snapshot.getValue<Contact>()!!
                Log.d("Added", snapshot.value.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                contacts.remove(snapshot.key)
                Log.d("Removed", snapshot.value.toString())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        return contacts
    }

    fun insert(key: String? = null, contact: Contact) {
        if (key != null) {
            Log.d("ID is not blank", key)
            contactCollectionReference.child(key).setValue(contact)
        } else {
            Log.d("ID is blank", contact.toString())
            contactCollectionReference.push().setValue(contact)
        }
    }

    fun addProductsForSupplier(key: String, product: Map<String, Double>) = contactCollectionReference.child("$key/product").setValue(product)

    fun removeProductForSupplier(contactId: String, productId: String) = contactCollectionReference.child("$contactId/product/$productId").removeValue()

    fun delete(key: String) = contactCollectionReference.child(key).removeValue()
}